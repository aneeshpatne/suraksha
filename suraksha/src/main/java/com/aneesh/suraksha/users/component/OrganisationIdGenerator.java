package com.aneesh.suraksha.users.component;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;

@Component
public class OrganisationIdGenerator {

    public String generateId() {
        String prefix = "org_";
        byte[] RandomBytes = new byte[30];
        new SecureRandom().nextBytes(RandomBytes);
        String RandomPart = Base64.getUrlEncoder().withoutPadding().encodeToString(RandomBytes);
        return prefix + RandomPart;

    }
}
