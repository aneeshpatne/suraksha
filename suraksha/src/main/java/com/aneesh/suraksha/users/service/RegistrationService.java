package com.aneesh.suraksha.users.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aneesh.suraksha.users.controller.Signup.SignupRequest;
import com.aneesh.suraksha.users.controller.Signup.SignupResult;
import com.aneesh.suraksha.users.dto.UserMetaData;
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

    public SignupResult OnBoard(SignupRequest entity, UserMetaData metaData) {
        try {
            Organisations organisation = organisationsRepository.findById(entity.organisationId()).orElse(null);
            if (organisation == null) {
                return new SignupResult(false, "An error occurred during registration", null, null, null);
            }
            UserEntity existing = userRepository.findByMailId(entity.mailId());
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
