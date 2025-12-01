package com.aneesh.suraksha.users.controller;

public class SignupResponse {
    private boolean status;
    private String message;

    public SignupResponse(boolean status, String message) {
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