package com.aneesh.suraksha.users.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = { "mailId", "organisation_id" }))
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mailId;
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisations organisations;

    public UserEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMailId(String mailId) {
        this.mailId = mailId;
    }

    public String getMailId() {
        return this.mailId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Organisations getOrganisations() {
        return organisations;
    }

    public void setOrganisations(Organisations organisations) {
        this.organisations = organisations;
    }

}