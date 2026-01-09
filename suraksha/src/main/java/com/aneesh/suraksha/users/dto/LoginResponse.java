package com.aneesh.suraksha.users.dto;

public record LoginResponse(String status, String message, String twoFactorToken) {
}
