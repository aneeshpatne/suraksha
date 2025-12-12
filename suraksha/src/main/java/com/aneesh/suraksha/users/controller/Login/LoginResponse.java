package com.aneesh.suraksha.users.controller.Login;

public record LoginResponse(Boolean status, String message, String jwt, String refreshToken) {
}
