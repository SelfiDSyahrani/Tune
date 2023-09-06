package com.example.tune.model.Register;

public class RegisterResponse {
    private String rc;
    private String msg;

    public RegisterResponse(String rc, String msg) {
        this.rc = rc;
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public String getRc() {
        return rc;
    }
}
