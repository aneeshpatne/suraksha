package com.aneesh.suraksha.users.dto;

import java.util.UUID;

public record VerifyMagicLinkResponse(UUID userId, Boolean status) {

}
