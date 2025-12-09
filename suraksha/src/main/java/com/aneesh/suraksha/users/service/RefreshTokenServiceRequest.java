package com.aneesh.suraksha.users.service;

import com.aneesh.suraksha.users.model.UserEntity;

public record RefreshTokenServiceRequest(UserEntity user, String ip, String userAgent) {
}