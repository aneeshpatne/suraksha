package com.aneesh.suraksha.users.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aneesh.suraksha.users.controller.Signup.SignupRequest;
import com.aneesh.suraksha.users.controller.Signup.SignupResponse;
import com.aneesh.suraksha.users.model.UserEntity;
import com.aneesh.suraksha.users.model.UserRepository;

@Service
public class HashService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public HashService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public SignupResponse OnBoard(SignupRequest entity) {
        UserEntity existing = userRepository.findByMailId(entity.getMailId());
        if (existing != null) {
            return new SignupResponse(false, "User Already Exists");
        }
        UserEntity user = new UserEntity();
        user.setMailId(entity.getMailId());
        String hashedPassword = passwordEncoder.encode(entity.getPassword());
        user.setPassword(hashedPassword);
        userRepository.save(user);
        return new SignupResponse(true, "User Created Successfully");
    }
}
