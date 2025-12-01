package com.aneesh.suraksha.users.controller.Login;

public class LoginRequest {
    private String email;
    private String password;

    public LoginRequest() {

    }

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }
}
