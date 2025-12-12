package com.aneesh.suraksha.users.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aneesh.suraksha.users.dto.SignupRequest;
import com.aneesh.suraksha.users.dto.SignupResult;
import com.aneesh.suraksha.users.dto.RefreshTokenServiceRequest;
import com.aneesh.suraksha.users.dto.UserMetaData;
import com.aneesh.suraksha.users.model.UserEntity;
import com.aneesh.suraksha.users.model.UserRepository;

import com.aneesh.suraksha.users.model.Organisations;
import com.aneesh.suraksha.users.model.OrganisationsRepository;

@Service
public class RegistrationService {

    private final RefreshTokenService refreshTokenService;

    private final JwtService jwtService;

    private final ApiKeyService apiKeyService;

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private OrganisationsRepository organisationsRepository;

    public RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            OrganisationsRepository organisationsRepository, JwtService jwtService,
            RefreshTokenService refreshTokenService, ApiKeyService apiKeyService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.organisationsRepository = organisationsRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.apiKeyService = apiKeyService;
    }

    public SignupResult OnBoard(SignupRequest entity, UserMetaData metaData) {
        try {
            Organisations organisation = organisationsRepository.findById(entity.organisationId()).orElse(null);
            if (organisation == null) {
                return new SignupResult(false, "An error occurred during registration", null, null, null);
            }
            // Verify API key before allowing registration
            boolean apiKeyMatch = apiKeyService.verifyApiKey(entity.organisationId(), entity.apiKey());
            if (!apiKeyMatch) {
                return new SignupResult(false, "Invalid API key", null, null, null);
            }
            // Check for existing user with same email AND organisation (allows same email
            // in different orgs)
            UserEntity existing = userRepository.findByMailIdAndOrganisationsId(entity.mailId(),
                    entity.organisationId());
            if (existing != null) {
                return new SignupResult(false, "User Already Exists", null, null, null);
            }
            UserEntity user = new UserEntity();
            user.setMailId(entity.mailId());
            user.setOrganisations(organisation);
            String hashedPassword = passwordEncoder.encode(entity.password());
            user.setPassword(hashedPassword);
            userRepository.save(user);
            String token = jwtService.generateToken(user);
            String refreshToken = refreshTokenService
                    .generate(new RefreshTokenServiceRequest(user, metaData.ip(), metaData.userAgent()));
            return new SignupResult(true, "User Created Successfully", token, refreshToken, user);
        } catch (Exception e) {
            return new SignupResult(false, "An error occurred during registration", null, null, null);
        }
    }
}
