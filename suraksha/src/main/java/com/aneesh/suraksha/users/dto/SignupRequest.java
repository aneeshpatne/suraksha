package com.aneesh.suraksha.users.dto;

public record SignupRequest(String mailId, String password, String organisationId, String apiKey) {
}
