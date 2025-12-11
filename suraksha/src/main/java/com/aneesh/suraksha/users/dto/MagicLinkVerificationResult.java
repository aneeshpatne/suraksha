package com.aneesh.suraksha.users.dto;

import java.util.UUID;

public record MagicLinkVerificationResult(Boolean status, UUID userId) {

}
