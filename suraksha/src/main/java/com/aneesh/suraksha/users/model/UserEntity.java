package com.aneesh.suraksha.users.model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.GeneratedValue;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = { "mailId", "organisation_id" }))
public class UserEntity {

    @Id
    @GeneratedValue
    private UUID id;

    private String mailId;
    private String password;
    private String twoFaType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisations organisations;

    public UserEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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

    public String getTwoFaType() {
        return twoFaType;
    }

    public void setTwoFaType(String twoFaType) {
        this.twoFaType = twoFaType;
    }

    public Organisations getOrganisations() {
        return organisations;
    }

    public void setOrganisations(Organisations organisations) {
        this.organisations = organisations;
    }

}