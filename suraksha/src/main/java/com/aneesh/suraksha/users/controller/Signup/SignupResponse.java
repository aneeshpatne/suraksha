package com.aneesh.suraksha.users.controller.Signup;

public record SignupResponse(Boolean status, String message, String jwt, String refreshToken) {
}