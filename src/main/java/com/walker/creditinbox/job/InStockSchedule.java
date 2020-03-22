package com.walker.creditinbox.job;

import com.walker.creditinbox.ApplicationParameter;
import com.walker.creditinbox.service.CreditModelParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class InStockSchedule {

    private final CreditModelParserService creditModelParserService;

    @Async(ApplicationParameter.InBoxThreadName)
    @Scheduled(fixedRate = 60000*3)
    public void scanAndParseFiles() {
        creditModelParserService.scanFile();
    }

    @PostConstruct
    public void parseFile() {
        log.info("【开始解析队列中的文件】");
        creditModelParserService.parseFile();
    }
}
