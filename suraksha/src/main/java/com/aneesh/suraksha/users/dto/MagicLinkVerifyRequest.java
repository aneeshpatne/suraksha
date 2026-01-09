package com.aneesh.suraksha.users.dto;

public record MagicLinkVerifyRequest(String token, String redirect) {

}
