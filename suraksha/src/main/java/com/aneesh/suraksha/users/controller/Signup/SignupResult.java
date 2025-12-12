package com.aneesh.suraksha.users.controller.Signup;

import com.aneesh.suraksha.users.model.UserEntity;

public record SignupResult(boolean status, String message, String token, String refreshToken, UserEntity user) {
}