package com.aneesh.suraksha.users.dto;

import java.util.UUID;

import com.aneesh.suraksha.users.model.UserEntity;

public record UserDTO(UUID id, String mailId, String organisationId) {

    public static UserDTO fromEntity(UserEntity user) {
        return new UserDTO(
                user.getId(),
                user.getMailId(),
                user.getOrganisations().getId());
    }
}
