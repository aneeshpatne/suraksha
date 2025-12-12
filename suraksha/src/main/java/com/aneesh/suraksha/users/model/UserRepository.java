package com.aneesh.suraksha.users.model;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aneesh.suraksha.users.dto.UserDto;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    UserEntity findByMailId(String mailId);

    UserEntity findByMailIdAndOrganisationsId(String mailId, String organisationsId);

    @Query("SELECT new com.aneesh.suraksha.users.dto.UserDto(u.id, u.mailId, u.organisations.id) FROM UserEntity u")
    List<UserDto> findAllUsersAsDto();
}
