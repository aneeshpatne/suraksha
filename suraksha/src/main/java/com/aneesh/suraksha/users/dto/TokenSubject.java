package com.aneesh.suraksha.users.dto;

import java.util.UUID;

import com.aneesh.suraksha.users.model.UserEntity;

public record TokenSubject(UUID userId, String mailId, String organisationId) {
    public static TokenSubject fromUser(UserEntity user) {
        return new TokenSubject(
                user.getId(),
                user.getMailId(),
                user.getOrganisations().getId());
    }
}
