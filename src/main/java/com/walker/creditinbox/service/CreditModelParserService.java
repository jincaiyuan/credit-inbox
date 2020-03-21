package com.walker.creditinbox.service;

import com.walker.creditinbox.model.CreditModel;

import java.util.List;

public interface CreditModelParserService {

    void parseFile();
    List<CreditModel> listCredits();
}
