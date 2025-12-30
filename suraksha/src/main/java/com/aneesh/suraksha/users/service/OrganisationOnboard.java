package com.aneesh.suraksha.users.service;

import org.springframework.stereotype.Service;

import com.aneesh.suraksha.users.component.OrganisationIdGenerator;
import com.aneesh.suraksha.users.dto.CreateOrganizationRequest;
import com.aneesh.suraksha.users.dto.CreateOrganizationResponse;
import com.aneesh.suraksha.users.model.Organisations;
import com.aneesh.suraksha.users.model.OrganisationsRepository;

@Service
public class OrganisationOnboard {

    private final OrganisationIdGenerator organisationIdGenerator;
    private final OrganisationsRepository organisationsRepository;

    public OrganisationOnboard(OrganisationIdGenerator organisationIdGenerator,
            OrganisationsRepository organisationsRepository) {
        this.organisationIdGenerator = organisationIdGenerator;
        this.organisationsRepository = organisationsRepository;
    }

    public CreateOrganizationResponse OnBoard(CreateOrganizationRequest req) {
        String ID = organisationIdGenerator.generateId();
        Organisations organisation = new Organisations(ID, req.name());
        CreateOrganizationResponse res = new CreateOrganizationResponse(ID, req.name());
        organisationsRepository.save(organisation);
        return res;
    }

}
