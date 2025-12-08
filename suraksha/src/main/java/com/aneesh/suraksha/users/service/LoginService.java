package com.aneesh.suraksha.users.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aneesh.suraksha.users.controller.Login.LoginRequest;
import com.aneesh.suraksha.users.model.UserEntity;
import com.aneesh.suraksha.users.model.UserRepository;

@Service
public class LoginService {

    private final JwtService jwtService;

    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    public LoginService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResult login(LoginRequest request) {
        try {
            UserEntity user = userRepository.findByMailId(request.mailId());
            if (user == null) {
                return new LoginResult(false, "Invalid credentials", null);
            }
            if (user.getOrganisations() == null || !user.getOrganisations().getId().equals(request.organisationId())) {
                return new LoginResult(false, "Invalid credentials", null);
            }
            boolean match = passwordEncoder.matches(request.password(), user.getPassword());
            if (!match) {
                return new LoginResult(false, "Invalid credentials", null);
            }
            String token = jwtService.generateToken(user);
            return new LoginResult(true, "Success", token);
        } catch (Exception e) {
            logger.error("Error during login for user: {}", request.mailId(), e);
            return new LoginResult(false, "Invalid credentials", null);
        }
    }
}
