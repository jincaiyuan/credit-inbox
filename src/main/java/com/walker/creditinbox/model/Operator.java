package com.walker.creditinbox.model;

public enum Operator {
    CTCC("CTCC", 0),
    CMCC("CMCC", 1),
    CUCC("CUCC", 2);

    private String cooperate;
    private int code;

    Operator(String cooperate, int code) {
        this.cooperate = cooperate;
        this.code = code;
    }

    public String getCooperate() {
        return cooperate;
    }

    public int getCode() {
        return code;
    }
}
