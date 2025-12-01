package com.aneesh.suraksha.users.controller;

public class LoginResponse {
    private boolean status;
    private String message;

    public LoginResponse(boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    public boolean getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }
}