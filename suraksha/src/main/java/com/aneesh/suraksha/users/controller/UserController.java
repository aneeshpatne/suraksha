package com.aneesh.suraksha.users.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.aneesh.suraksha.users.controller.Login.LoginRequest;
import com.aneesh.suraksha.users.controller.Signup.SignupResponse;
import com.aneesh.suraksha.users.model.UserEntity;
import com.aneesh.suraksha.users.model.UserRepository;
import com.aneesh.suraksha.users.service.HashService;

import java.util.List;

@RestController
public class UserController {
    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final HashService hashService;

    public UserController(UserRepository userRepository, HashService hashService) {
        this.userRepository = userRepository;
        this.hashService = hashService;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> createUser(@RequestBody UserEntity userEntity) {
        SignupResponse res = hashService.OnBoard(userEntity);
        return ResponseEntity.status(res.getStatus() ? HttpStatus.OK : HttpStatus.FORBIDDEN).body(res);
    }

    @GetMapping("/users")
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/login")
    public void postMethodName(@RequestBody LoginRequest entity) {
        System.out.println(entity.getMailId());
        System.out.println(entity.getPassword());
    }

}
