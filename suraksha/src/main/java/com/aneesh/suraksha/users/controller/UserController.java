package com.aneesh.suraksha.users.controller;

import com.aneesh.suraksha.users.service.LoginService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.aneesh.suraksha.users.controller.Login.LoginRequest;
import com.aneesh.suraksha.users.controller.Login.LoginResponse;
import com.aneesh.suraksha.users.controller.Signup.SignupResponse;
import com.aneesh.suraksha.users.model.UserEntity;
import com.aneesh.suraksha.users.model.UserRepository;
import com.aneesh.suraksha.users.service.HashService;

import java.util.List;

@RestController
public class UserController {

    private final LoginService loginService;

    private final UserRepository userRepository;

    private final HashService hashService;

    public UserController(UserRepository userRepository, HashService hashService, LoginService loginService) {
        this.userRepository = userRepository;
        this.hashService = hashService;
        this.loginService = loginService;
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
    public ResponseEntity<LoginResponse> postMethodName(@Valid @RequestBody LoginRequest entity) {
        LoginResponse res = loginService.login(entity);
        return ResponseEntity.status(res.getStatus() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED).body(res);
    }

}
