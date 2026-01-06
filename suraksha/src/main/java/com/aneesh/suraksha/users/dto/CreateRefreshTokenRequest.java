package com.aneesh.suraksha.users.dto;

public record CreateRefreshTokenRequest(TokenSubject subject, String ip, String userAgent) {
}
