package com.aneesh.suraksha.users.dto;

import java.util.UUID;

public record MagicLinkVerifyResponse(UUID userId, Boolean status) {

}
