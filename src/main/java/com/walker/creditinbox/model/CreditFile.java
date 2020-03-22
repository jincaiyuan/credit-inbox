package com.walker.creditinbox.model;

import lombok.Data;

@Data
public class CreditFile {
    private String filename;
    private String batch;
    private String scanTime;
    private String scanDate;
    private HandleStatus status = HandleStatus.UNHANDLE;
}
