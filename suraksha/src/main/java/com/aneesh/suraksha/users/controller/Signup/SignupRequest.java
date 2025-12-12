package com.aneesh.suraksha.users.controller.Signup;

public record SignupRequest(String mailId, String password, String organisationId, String apiKey) {
}
