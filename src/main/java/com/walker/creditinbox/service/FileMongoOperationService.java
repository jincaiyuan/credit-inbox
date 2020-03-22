package com.walker.creditinbox.service;

import com.mongodb.client.result.UpdateResult;
import com.walker.creditinbox.model.CreditFile;

import java.util.List;

public interface FileMongoOperationService {
    List<CreditFile> queryCreditFileByNameAndDate(String filename, String scanDate);
    UpdateResult updateCreditFileHandleStatus(CreditFile creditFile);
}
