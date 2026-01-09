package com.aneesh.suraksha.users.dto;

public record TwoFactorAuthData(TokenSubject tokenSubject, String otp) {
}
