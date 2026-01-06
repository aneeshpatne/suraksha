package com.aneesh.suraksha.users.dto;

import java.util.UUID;

import com.aneesh.suraksha.users.model.UserEntity;

/**
 * DTO containing user identity fields needed for token generation.
 * Used by both JwtService and RefreshTokenService to decouple from UserEntity.
 */
public record TokenSubject(UUID userId, String mailId, String organisationId) {

    /**
     * Factory method to create TokenSubject from UserEntity.
     */
    public static TokenSubject fromUser(UserEntity user) {
        return new TokenSubject(
                user.getId(),
                user.getMailId(),
                user.getOrganisations().getId());
    }
}
