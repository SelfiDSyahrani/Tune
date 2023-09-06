package com.example.tune.model.login;

public class LoginResponse {
    private String access_token;
    private String rc;
    private String refresh_token;

    public String getAccess_token() {
        return access_token;
    }

    public String getRc() {
        return rc;
    }

    public String getRefresh_token() {
        return refresh_token;
    }
}
