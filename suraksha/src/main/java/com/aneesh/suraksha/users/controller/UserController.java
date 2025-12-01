package com.aneesh.suraksha.users.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.aneesh.suraksha.users.model.UserEntity;
import com.aneesh.suraksha.users.model.UserRepository;

import java.util.List;

@RestController
public class UserController {
    @Autowired
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> createUser(@RequestBody UserEntity userEntity) {
        UserEntity existingUser = userRepository.findByMailId(userEntity.getMailId());
        if (existingUser != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new LoginResponse(false, "Operation Failed"));
        }
        userRepository.save(userEntity);
        return ResponseEntity.status(HttpStatus.OK).body(new LoginResponse(true, "User Created Successfully"));
    }

    @GetMapping("/users")
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }
}

class LoginResponse {
    private boolean status;
    private String message;

    public LoginResponse(boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    public boolean getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }
}