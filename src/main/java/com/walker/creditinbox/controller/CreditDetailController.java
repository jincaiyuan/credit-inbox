package com.walker.creditinbox.controller;

import com.walker.creditinbox.model.CreditFile;
import com.walker.creditinbox.model.CreditModel;
import com.walker.creditinbox.service.CreditModelParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/credit")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CreditDetailController {

    private final CreditModelParserService creditModelParserService;

    @CrossOrigin
    @GetMapping("/files/detail")
    public List<CreditModel> showCredits() {
        return creditModelParserService.listCredits();
    }

    @CrossOrigin
    @GetMapping("/files/list")
    public List<CreditFile> showCreditFiles() {
        return creditModelParserService.listCreditFiles();
    }
}
