package com.aneesh.suraksha.users.dto;

public record RegisterResponse(Boolean status, String message, String jwt, String refreshToken) {
}
