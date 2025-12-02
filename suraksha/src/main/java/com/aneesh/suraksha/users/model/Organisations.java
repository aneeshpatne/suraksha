package com.aneesh.suraksha.users.model;

import java.security.SecureRandom;

import jakarta.persistence.Entity;

import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "organisations")
public class Organisations {
    @Id
    private String id;
    private String name;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String BASE62 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public Organisations() {
        this.id = "org_" + secureRandomString(12);
    }

    private String secureRandomString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(BASE62.charAt(secureRandom.nextInt(BASE62.length())));
        }
        return sb.toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
