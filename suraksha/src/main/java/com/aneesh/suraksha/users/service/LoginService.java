package com.aneesh.suraksha.users.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aneesh.suraksha.users.dto.AuthResult;
import com.aneesh.suraksha.users.dto.LoginRequest;
import com.aneesh.suraksha.users.dto.TokenSubject;
import com.aneesh.suraksha.users.model.UserEntity;
import com.aneesh.suraksha.users.model.UserRepository;

@Service
public class LoginService {

    private final TwofactorService twofactorService;

    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    public LoginService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            TwofactorService twofactorService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.twofactorService = twofactorService;
    }

    public AuthResult authenticate(LoginRequest request) {
        try {
            UserEntity user = userRepository.findByMailId(request.mailId());
            if (user == null) {
                return AuthResult.failure("Invalid credentials");
            }
            if (user.getOrganisations() == null || !user.getOrganisations().getId().equals(request.organisationId())) {
                return AuthResult.failure("Invalid credentials");
            }
            boolean match = passwordEncoder.matches(request.password(), user.getPassword());
            if (!match) {
                return AuthResult.failure("Invalid credentials");
            }
            TokenSubject subject = TokenSubject.fromUser(user);
            if (user.getTwoFaType().equals("otp")) {
                String token = twofactorService.Generate(subject);
                if (token != null) {
                    return AuthResult.two_fa_required(token);
                }
            }
            return AuthResult.success(subject);
        } catch (Exception e) {
            logger.error("Error during authentication for user: {}", request.mailId(), e);
            return AuthResult.failure("Invalid credentials");
        }
    }
}
