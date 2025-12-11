package com.aneesh.suraksha.users.dto;

import java.util.UUID;

public record MagicLinkTokenPayload(UUID userId, long createdAt) {

}
