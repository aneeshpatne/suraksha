package com.aneesh.suraksha.users.service;

import com.aneesh.suraksha.users.model.UserEntity;

public record LoginResult(Boolean status, String message, String token, String refreshToken, UserEntity user) {
}
