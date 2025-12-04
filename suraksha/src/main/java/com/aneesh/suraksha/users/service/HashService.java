package com.aneesh.suraksha.users.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aneesh.suraksha.users.controller.Signup.SignupRequest;
import com.aneesh.suraksha.users.controller.Signup.SignupResponse;
import com.aneesh.suraksha.users.model.UserEntity;
import com.aneesh.suraksha.users.model.UserRepository;

import jakarta.servlet.http.HttpServletResponse;

import com.aneesh.suraksha.users.model.Organisations;
import com.aneesh.suraksha.users.model.OrganisationsRepository;

@Service
public class HashService {

    private final JwtService jwtService;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private OrganisationsRepository organisationsRepository;

    public HashService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            OrganisationsRepository organisationsRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.organisationsRepository = organisationsRepository;
        this.jwtService = jwtService;
    }

    public SignupResponse OnBoard(SignupRequest entity, HttpServletResponse response) {
        Organisations organisation = organisationsRepository.findById(entity.organisationId()).orElse(null);
        if (organisation == null) {
            return new SignupResponse(false, "Organisation Not Found");
        }
        UserEntity existing = userRepository.findByMailId(entity.mailId());
        if (existing != null) {
            return new SignupResponse(false, "User Already Exists");
        }
        UserEntity user = new UserEntity();
        user.setMailId(entity.mailId());
        user.setOrganisations(organisation);
        String hashedPassword = passwordEncoder.encode(entity.password());
        user.setPassword(hashedPassword);
        userRepository.save(user);
        String accessToken = jwtService.generateToken(user);
        String refreshToken = refresh
        return new SignupResponse(true, "User Created Successfully");
    }
}
