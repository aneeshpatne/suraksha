package com.aneesh.suraksha.users.controller.Login;

public record LoginRequest(String mailId, String password, String organisationId, String apiKey) {
}
