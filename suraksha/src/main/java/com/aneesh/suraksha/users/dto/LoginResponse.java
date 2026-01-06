package com.aneesh.suraksha.users.dto;

public record LoginResponse(Boolean status, String message, String jwt) {
}
