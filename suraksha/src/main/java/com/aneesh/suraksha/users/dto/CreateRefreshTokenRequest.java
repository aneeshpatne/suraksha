package com.aneesh.suraksha.users.dto;

import com.aneesh.suraksha.users.model.UserEntity;

public record CreateRefreshTokenRequest(UserEntity user, String ip, String userAgent) {
}
