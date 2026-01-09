package com.aneesh.suraksha.users.dto;

public record AuthResult(Boolean success, String message, TokenSubject subject) {
    public static AuthResult failure(String message) {
        return new AuthResult(false, message, null);
    }

    public static AuthResult two_fa_required() {
        return new AuthResult(false, "TwoFA Required", null);
    }

    public static AuthResult success(TokenSubject subject) {
        return new AuthResult(true, "Success", subject);
    }
}
