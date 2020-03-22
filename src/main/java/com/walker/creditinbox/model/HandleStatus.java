package com.walker.creditinbox.model;

public enum HandleStatus {
    UNHANDLE("未处理", 0),
    HANDLING("正在处理", 1),
    HANDLED("处理完成", 2),
    EXCEPTION("处理异常", 3)
    ;

    private String status;
    private int code;

    HandleStatus(String status, int code) {
        this.status = status;
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public int getCode() {
        return code;
    }
}
