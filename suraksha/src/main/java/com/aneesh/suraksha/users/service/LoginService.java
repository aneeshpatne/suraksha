package com.aneesh.suraksha.users.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aneesh.suraksha.users.dto.LoginRequest;
import com.aneesh.suraksha.users.dto.LoginResult;
import com.aneesh.suraksha.users.dto.CreateRefreshTokenRequest;
import com.aneesh.suraksha.users.dto.TokenSubject;
import com.aneesh.suraksha.users.dto.RequestMetadata;
import com.aneesh.suraksha.users.model.UserEntity;
import com.aneesh.suraksha.users.model.UserRepository;

@Service
public class LoginService {

    private final RefreshTokenService refreshTokenService;

    private final JwtService jwtService;

    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    public LoginService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
            RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public LoginResult login(LoginRequest request, RequestMetadata metaData) {
        try {
            UserEntity user = userRepository.findByMailId(request.mailId());
            if (user == null) {
                return new LoginResult(false, "Invalid credentials", null, null, null);
            }
            if (user.getOrganisations() == null || !user.getOrganisations().getId().equals(request.organisationId())) {
                return new LoginResult(false, "Invalid credentials", null, null, null);
            }
            boolean match = passwordEncoder.matches(request.password(), user.getPassword());
            if (!match) {
                return new LoginResult(false, "Invalid credentials", null, null, null);
            }

            TokenSubject subject = TokenSubject.fromUser(user);
            String token = jwtService.generateToken(subject);
            String refreshToken = refreshTokenService
                    .generate(new CreateRefreshTokenRequest(subject, metaData.ip(), metaData.userAgent()));
            return new LoginResult(true, "Success", token, refreshToken, user);
        } catch (Exception e) {
            logger.error("Error during login for user: {}", request.mailId(), e);
            return new LoginResult(false, "Invalid credentials", null, null, null);
        }
    }
}
