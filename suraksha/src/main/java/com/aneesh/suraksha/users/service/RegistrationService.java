package com.aneesh.suraksha.users.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aneesh.suraksha.users.dto.RegisterRequest;
import com.aneesh.suraksha.users.dto.RegisterResult;
import com.aneesh.suraksha.users.dto.CreateRefreshTokenRequest;
import com.aneesh.suraksha.users.dto.TokenSubject;
import com.aneesh.suraksha.users.dto.RequestMetadata;
import com.aneesh.suraksha.users.model.UserEntity;
import com.aneesh.suraksha.users.model.UserRepository;

import com.aneesh.suraksha.users.model.Organisations;
import com.aneesh.suraksha.users.model.OrganisationsRepository;

@Service
public class RegistrationService {

    private final RefreshTokenService refreshTokenService;

    private final JwtService jwtService;

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private OrganisationsRepository organisationsRepository;

    public RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            OrganisationsRepository organisationsRepository, JwtService jwtService,
            RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.organisationsRepository = organisationsRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public RegisterResult OnBoard(RegisterRequest entity, RequestMetadata metaData) {
        try {
            Organisations organisation = organisationsRepository.findById(entity.organisationId()).orElse(null);
            if (organisation == null) {
                return new RegisterResult(false, "Organisation not found", null, null, null);
            }
            // Check for existing user with same email AND organisation (allows same email
            // in different orgs)
            UserEntity existing = userRepository.findByMailIdAndOrganisationsId(entity.mailId(),
                    entity.organisationId());
            if (existing != null) {
                return new RegisterResult(false, "User Already Exists", null, null, null);
            }
            UserEntity user = new UserEntity();
            user.setMailId(entity.mailId());
            user.setOrganisations(organisation);
            String hashedPassword = passwordEncoder.encode(entity.password());
            user.setPassword(hashedPassword);
            userRepository.save(user);
            TokenSubject subject = TokenSubject.fromUser(user);
            String token = jwtService.generateToken(subject);
            String refreshToken = refreshTokenService
                    .generate(new CreateRefreshTokenRequest(subject, metaData.ip(), metaData.userAgent()));
            return new RegisterResult(true, "User Created Successfully", token, refreshToken, user);
        } catch (Exception e) {
            return new RegisterResult(false, "An error occurred during registration", null, null, null);
        }
    }
}
