package com.aneesh.suraksha.users.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aneesh.suraksha.users.dto.UserDTO;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByMailId(String mailId);

    @Query("SELECT new com.aneesh.suraksha.users.dto.UserDTO(u.id, u.mailId, u.organisations.id) FROM UserEntity u")
    List<UserDTO> findAllUsersAsDTO();
}
