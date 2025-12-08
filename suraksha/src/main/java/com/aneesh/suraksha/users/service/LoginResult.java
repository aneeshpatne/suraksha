package com.aneesh.suraksha.users.service;

public record LoginResult(Boolean status, String message, String token) {
}
