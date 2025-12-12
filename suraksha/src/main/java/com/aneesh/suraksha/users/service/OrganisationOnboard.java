package com.aneesh.suraksha.users.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aneesh.suraksha.users.component.OrganisationIdGenerator;
import com.aneesh.suraksha.users.model.Organisations;
import com.aneesh.suraksha.users.model.OrganisationsRepository;

@Service
public class OrganisationOnboard {

    private final PasswordEncoder passwordEncoder;
    private final OrganisationIdGenerator organisationIdGenerator;
    private final OrganisationsRepository organisationsRepository;
    private final ApiKeyService apiKeyService;

    public OrganisationOnboard(OrganisationIdGenerator organisationIdGenerator,
            OrganisationsRepository organisationsRepository, ApiKeyService apiKeyService,
            PasswordEncoder passwordEncoder) {
        this.organisationIdGenerator = organisationIdGenerator;
        this.organisationsRepository = organisationsRepository;
        this.apiKeyService = apiKeyService;
        this.passwordEncoder = passwordEncoder;
    }

    public OnboardResponse OnBoard(OnboardRequest req) {
        String API_KEY = apiKeyService.generateAPIKey();
        String ID = organisationIdGenerator.generateId();
        String HashedKey = apiKeyService.hashAPIKey(API_KEY);
        Organisations organisation = new Organisations(ID, req.name(), HashedKey);
        OnboardResponse res = new OnboardResponse(ID, req.name(), API_KEY);
        organisationsRepository.save(organisation);
        return res;
    }

}
