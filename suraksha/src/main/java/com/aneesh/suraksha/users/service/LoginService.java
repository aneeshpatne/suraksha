package com.aneesh.suraksha.users.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aneesh.suraksha.users.controller.Login.LoginRequest;
import com.aneesh.suraksha.users.dto.UserMetaData;
import com.aneesh.suraksha.users.model.UserEntity;
import com.aneesh.suraksha.users.model.UserRepository;

@Service
public class LoginService {

    private final ApiKeyService apiKeyService;

    private final RefreshTokenService refreshTokenService;

    private final JwtService jwtService;

    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    public LoginService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
            RefreshTokenService refreshTokenService, ApiKeyService apiKeyService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.apiKeyService = apiKeyService;
    }

    public LoginResult login(LoginRequest request, UserMetaData metaData) {
        try {
            UserEntity user = userRepository.findByMailId(request.mailId());
            if (user == null) {
                return new LoginResult(false, "Invalid credentials", null, null, null);
            }
            if (user.getOrganisations() == null || !user.getOrganisations().getId().equals(request.organisationId())) {
                return new LoginResult(false, "Invalid credentials", null, null, null);
            }
            boolean match = passwordEncoder.matches(request.password(), user.getPassword());
            boolean apiKeyMatch = apiKeyService.verifyApiKey(user.getOrganisations().getId(),
                    request.apiKey());
            if (!match || !apiKeyMatch) {
                return new LoginResult(false, "Invalid credentials", null, null, null);
            }

            String token = jwtService.generateToken(user);
            String refreshToken = refreshTokenService
                    .generate(new RefreshTokenServiceRequest(user, metaData.ip(), metaData.userAgent()));
            return new LoginResult(true, "Success", token, refreshToken, user);
        } catch (Exception e) {
            logger.error("Error during login for user: {}", request.mailId(), e);
            return new LoginResult(false, "Invalid credentials", null, null, null);
        }
    }
}
