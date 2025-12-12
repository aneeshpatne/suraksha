package com.aneesh.suraksha.users.dto;

public record SignupResponse(Boolean status, String message, String jwt, String refreshToken) {
}
