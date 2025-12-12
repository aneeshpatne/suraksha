package com.aneesh.suraksha.users.dto;

import java.util.UUID;

import com.aneesh.suraksha.users.model.UserEntity;

public record UserDto(UUID id, String mailId, String organisationId) {

    public static UserDto fromEntity(UserEntity user) {
        return new UserDto(
                user.getId(),
                user.getMailId(),
                user.getOrganisations().getId());
    }
}
