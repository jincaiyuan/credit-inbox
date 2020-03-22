package com.walker.creditinbox.service;

import com.walker.creditinbox.model.CreditFile;
import com.walker.creditinbox.model.CreditModel;

import java.util.List;

public interface CreditModelParserService {
    void scanFile();
    void parseFile();
    List<CreditFile> listCreditFiles();
    List<CreditModel> listCredits();
}
