package com.walker.creditinbox.service.impl;

import com.walker.creditinbox.model.CreditFile;
import com.walker.creditinbox.model.CreditModel;
import com.walker.creditinbox.model.HandleStatus;
import com.walker.creditinbox.model.Operator;
import com.walker.creditinbox.service.CreditModelParserService;
import com.walker.creditinbox.service.FileMongoOperationService;
import com.walker.creditinbox.util.DateUtil;
import com.walker.creditinbox.util.MurmurHash;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.NumberUtils;

import javax.annotation.PostConstruct;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CreditModelParserServiceImpl implements CreditModelParserService {

    @Value("${inbox.localPath}")
    private String localPath;

    @Value("${redis.expireTime}")
    private int expireTime;

    @Value("${mongo.storage.prefix}")
    private String mongoInPrefix;

    @Value("${mongo_file_db}")
    private String mongoFileDB;

    private final FileMongoOperationService fileMongoOperationService;
    private final RedisTemplate<String, String> redisTemplate;
    private final MongoTemplate mongoTemplate;

    private ConcurrentLinkedQueue<CreditFile> creditFileLinkedQueue;
    private ExecutorService parseFileService;

    @PostConstruct
    public void init() {
        parseFileService = Executors.newFixedThreadPool(1);
        creditFileLinkedQueue = new ConcurrentLinkedQueue<>();
    }

    /**
     * 扫描文件夹下的文件，并将文件的完整路径等信息放到队列，并存入mongo
     */
    @Override
    public void scanFile() {
        String today = DateUtil.getCurrentDateString("yyyyMMdd");
        File dir = new File(localPath + today);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        log.info("【开始扫描文件夹：{} 】", dir.getAbsolutePath());
        File[] files = dir.listFiles();
        if (files == null) {
            log.info("没有文件可以扫描");
            return;
        }

        for (File file : files) {
            String filename = file.getAbsolutePath();
            String inDate = DateUtil.getCurrentDateString("yyyyMMdd");

            List<CreditFile> existFiles = fileMongoOperationService.queryCreditFileByNameAndDate(filename, inDate);
            if (existFiles.size() > 0) continue;
            String batch = "TEST_" + new Date().getTime() + "_" + MurmurHash.hash(file.getName().getBytes());
            String scanTime = DateUtil.getCurrentDateString("yyyyMMdd HH:mm:ss");

            CreditFile creditFile = new CreditFile();
            creditFile.setFilename(filename);
            creditFile.setBatch(batch);
            creditFile.setScanTime(scanTime);
            creditFile.setScanDate(inDate);
            creditFileLinkedQueue.add(creditFile);
            mongoTemplate.insert(creditFile, mongoFileDB);
        }
        if (mongoTemplate.indexOps(mongoFileDB).getIndexInfo().size() == 0) {
            mongoTemplate.indexOps(mongoFileDB).ensureIndex(new Index().on("filename", Sort.Direction.ASC));
            mongoTemplate.indexOps(mongoFileDB).ensureIndex(new Index().on("scanDate", Sort.Direction.ASC));
        }
    }

    /**
     * 从队列获取文件，解析内容并入库
     */
    @Override
    public void parseFile() {
        parseFileService.submit(() -> {
            while (true) {
                String today = DateUtil.getCurrentDateString("yyyyMMdd");
                CreditFile creditFile = creditFileLinkedQueue.poll();
                if (creditFile != null) {
                    String collectionName = mongoInPrefix + today;
                    File file = new File(creditFile.getFilename());
                    try {
                        FileUtils.readLines(file, Charset.defaultCharset()).forEach(line -> {
                            creditFile.setStatus(HandleStatus.HANDLING);
                            fileMongoOperationService.updateCreditFileHandleStatus(creditFile);
                            CreditModel creditModel = parseLine(line, file.getName());
                            if (creditModel != null) {
                                // 插入到mongodb，每天都新建一个集合
                                mongoTemplate.insert(creditModel, collectionName);
                            }
                        });
                        // 更新入库文件状态
                        creditFile.setStatus(HandleStatus.HANDLED);
                        fileMongoOperationService.updateCreditFileHandleStatus(creditFile);
                    } catch (IOException e) {
                        log.error(e.getMessage());
                        creditFile.setStatus(HandleStatus.EXCEPTION);
                        fileMongoOperationService.updateCreditFileHandleStatus(creditFile);
                    }
                    file.delete();

                    // 为 rySecret创建索引
                    if (mongoTemplate.indexOps(collectionName).getIndexInfo().size() == 0)
                        mongoTemplate.indexOps(collectionName).ensureIndex(new Index().on("rySecret", Sort.Direction.ASC));
                }
            }
        });
    }

    @Override
    public List<CreditFile> listCreditFiles() {
        return mongoTemplate.findAll(CreditFile.class, mongoFileDB);
    }

    @Override
    public List<CreditModel> listCredits() {
        List<CreditModel> models;
        String collections = mongoInPrefix + DateUtil.getCurrentDateString("yyyyMMdd");
        models = mongoTemplate.findAll(CreditModel.class, collections);
        return models;
    }

    /**
     * 解析入库文件每一行数据，返回入库数据对象
     *
     * @param line     入库文件中的一行
     * @param filename 入库文件名
     * @return 入库数据对象
     */
    private CreditModel parseLine(String line, String filename) {
        String batch = "TEST_" + new Date().getTime() + "_" + MurmurHash.hash(filename.getBytes());
        String inTime = DateUtil.getCurrentDateString("yyyyMMdd HH:mm:ss");
        String[] cells = line.split("\\|");
        if (cells.length == 6) {
            String rySecret = cells[0];
            // 使用redis判断是否重复，rySecret为键，文件名为值
            String duplication = redisTemplate.opsForValue().get(rySecret);
            if (duplication == null) {
                CreditModel creditModel = new CreditModel();
                creditModel.setFileName(filename);
                creditModel.setBatch(batch);
                creditModel.setRySecret(rySecret);
                redisTemplate.opsForValue().set(rySecret, filename, expireTime, TimeUnit.HOURS);
                String f1 = cells[1];
                creditModel.setF1(f1);
                String f2 = cells[2];
                creditModel.setF2(f2);
                Integer cityCode = NumberUtils.parseNumber(cells[3], Integer.class);
                creditModel.setCityCode(cityCode);
                Operator operator = null;
                int op = NumberUtils.parseNumber(cells[4], Integer.class);
                if (op == 0) operator = Operator.CTCC;
                if (op == 1) operator = Operator.CMCC;
                if (op == 2) operator = Operator.CUCC;
                creditModel.setOperator(operator);
                Integer mobileSuffix = NumberUtils.parseNumber(cells[5], Integer.class);
                creditModel.setMobileSuffix(mobileSuffix);
                creditModel.setInTime(inTime);
                return creditModel;
            } else {
                log.warn("【存在重复：{}】", rySecret);
                return null;
            }
        } else {
            log.error("文件：{} 内容不符合", filename);
            return null;
        }
    }
}
