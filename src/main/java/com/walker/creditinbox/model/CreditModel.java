package com.walker.creditinbox.model;

import lombok.Data;

@Data
public class CreditModel {
    private String fileName;
    private String rySecret;
    private String f1 = "";
    private String f2 = "";
    private Integer cityCode;
    private Operator operator;
    private Integer mobileSuffix;
    private String batch; //批次号
    private String inTime; // 入库时间
}
