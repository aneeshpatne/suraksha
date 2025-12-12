package com.aneesh.suraksha.users.dto;

public record LoginRequest(String mailId, String password, String organisationId, String apiKey) {
}
