package com.aneesh.suraksha.users.dto;

import java.util.UUID;

public record VerifySendMagicUrl(Boolean status, UUID userId) {

}
