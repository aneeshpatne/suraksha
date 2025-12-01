package com.aneesh.suraksha.users.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aneesh.suraksha.users.controller.LoginResponse;
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

    public LoginResponse OnBoard(UserEntity entity) {
        UserEntity existing = userRepository.findByMailId(entity.getMailId());
        if (existing != null) {
            return new LoginResponse(false, "User Already Exists");
        }
        UserEntity user = new UserEntity();
        user.setMailId(entity.getMailId());
        String hashedPassword = passwordEncoder.encode(entity.getPassword());
        userRepository.save(user);
        user.setPassword(hashedPassword);
        return new LoginResponse(true, "User Created Successfully");
    }
}
