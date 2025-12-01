package com.aneesh.suraksha.users.controller.Login;

public class LoginRequest {
    private String mailId;
    private String password;

    public LoginRequest() {

    }

    public String getMailId() {
        return this.mailId;
    }

    public String getPassword() {
        return this.password;
    }
}
