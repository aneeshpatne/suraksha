package com.aneesh.suraksha.users.controller.Signup;

public class SignupRequest {
    private String mailId;
    private String password;

    public SignupRequest(String mailId, String password) {
        this.mailId = mailId;
        this.password = password;
    }

    public String getMailId() {
        return this.mailId;
    }

    public String getPassword() {
        return this.password;
    }

}
