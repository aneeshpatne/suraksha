package com.aneesh.suraksha.users.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aneesh.suraksha.users.controller.Login.LoginRequest;
import com.aneesh.suraksha.users.controller.Login.LoginResponse;
import com.aneesh.suraksha.users.model.UserEntity;
import com.aneesh.suraksha.users.model.UserRepository;

@Service
public class LoginService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    public LoginService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByMailId(request.mailId());
        if (user == null) {
            return new LoginResponse(false, "User Does Not exist");
        }
        if (!user.getOrganisations().getId().equals(request.organisationId())) {
            return new LoginResponse(false, "User does not belong to this organisation");
        }
        boolean match = passwordEncoder.matches(request.password(), user.getPassword());
        if (!match) {
            return new LoginResponse(false, "Mail Id or Password is Wrong");
        }
        return new LoginResponse(true, "Success");
    }
}
