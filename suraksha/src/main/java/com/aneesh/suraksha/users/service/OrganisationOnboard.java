package com.aneesh.suraksha.users.service;

import org.springframework.stereotype.Service;

import com.aneesh.suraksha.users.component.ApiKeyGenerator;
import com.aneesh.suraksha.users.component.OrganisationIdGenerator;
import com.aneesh.suraksha.users.model.Organisations;
import com.aneesh.suraksha.users.model.OrganisationsRepository;

@Service
public class OrganisationOnboard {
    private final OrganisationIdGenerator organisationIdGenerator;
    private final OrganisationsRepository organisationsRepository;
    private final ApiKeyGenerator apiKeyGenerator;

    public OrganisationOnboard(OrganisationIdGenerator organisationIdGenerator,
            OrganisationsRepository organisationsRepository, ApiKeyGenerator apiKeyGenerator) {
        this.organisationIdGenerator = organisationIdGenerator;
        this.organisationsRepository = organisationsRepository;
        this.apiKeyGenerator = apiKeyGenerator;
    }

    public Organisations OnBoard(OnboardRequest req) {
        String API_KEY = apiKeyGenerator.generateAPIKey();
        String ID = organisationIdGenerator.generateId();
        Organisations organisation = new Organisations(ID, req.name, API_KEY);
        organisationsRepository.save(organisation);
        return organisation;
    }

}
