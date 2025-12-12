package com.aneesh.suraksha.users.model;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganisationsRepository extends JpaRepository<Organisations, String> {

    Optional<Organisations> findById(String id);

}