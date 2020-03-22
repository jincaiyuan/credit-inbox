package com.walker.creditinbox.service.impl;

import com.mongodb.client.result.UpdateResult;
import com.walker.creditinbox.model.CreditFile;
import com.walker.creditinbox.service.FileMongoOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;


@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class FileMongoOperationServiceImpl implements FileMongoOperationService {

    @Value("${mongo_file_db}")
    private String mongoFileDB;

    private final MongoTemplate mongoTemplate;

    @Override
    public List<CreditFile> queryCreditFileByNameAndDate(String filename, String scanDate) {
        return mongoTemplate.find(query(where("filename").is(filename).and("scanDate").is(scanDate)),
                CreditFile.class, mongoFileDB);
    }

    @Override
    public UpdateResult updateCreditFileHandleStatus(CreditFile creditFile) {
        return mongoTemplate.updateMulti(query(where("filename").is(creditFile.getFilename()).and("scanDate").is(creditFile.getScanDate())),
                new Update().set("status", creditFile.getStatus()), CreditFile.class, mongoFileDB);
    }
}
