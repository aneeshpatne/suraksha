package com.aneesh.suraksha.users.controller.Login;

public class LoginResponse {
    private Boolean status;
    private String message;

    public LoginResponse(Boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    public Boolean getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }
}
