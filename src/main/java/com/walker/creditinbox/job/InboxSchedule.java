package com.walker.creditinbox.job;

import com.walker.creditinbox.service.CreditModelParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class InboxSchedule {

    private final CreditModelParserService creditModelParserService;

    @Scheduled(fixedRate = 60000*3)
    public void scanAndParseFiles() {
        creditModelParserService.parseFile();
    }
}
