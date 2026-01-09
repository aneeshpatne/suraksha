package com.aneesh.suraksha.users.dto;

public record AuthResult(String status, String message, TokenSubject subject, String token) {
    public static AuthResult failure(String message) {
        return new AuthResult("false", message, null, null);
    }

    public static AuthResult two_fa_required(String token) {
        return new AuthResult("2fa", "TwoFA Required", null, token);
    }

    public static AuthResult success(TokenSubject subject) {
        return new AuthResult("true", "Success", subject, null);
    }
}
