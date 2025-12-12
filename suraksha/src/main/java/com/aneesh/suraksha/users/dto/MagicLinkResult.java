package com.aneesh.suraksha.users.dto;

import java.util.UUID;

public record MagicLinkResult(Boolean status, UUID userId) {

}
