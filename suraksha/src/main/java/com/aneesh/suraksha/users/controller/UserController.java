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
import com.aneesh.suraksha.users.controller.Signup.SignupRequest;
import com.aneesh.suraksha.users.controller.Signup.SignupResponse;
import com.aneesh.suraksha.users.model.Organisations;
import com.aneesh.suraksha.users.model.OrganisationsRepository;
import com.aneesh.suraksha.users.model.UserEntity;
import com.aneesh.suraksha.users.model.UserRepository;
import com.aneesh.suraksha.users.service.HashService;

import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class UserController {

    private final OrganisationsRepository organisationsRepository;

    private final LoginService loginService;

    private final UserRepository userRepository;

    private final HashService hashService;

    public UserController(UserRepository userRepository, HashService hashService, LoginService loginService,
            OrganisationsRepository organisationsRepository) {
        this.userRepository = userRepository;
        this.hashService = hashService;
        this.loginService = loginService;
        this.organisationsRepository = organisationsRepository;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> createUser(@RequestBody SignupRequest entity) {
        SignupResponse res = hashService.OnBoard(entity);
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

    @PostMapping("/auth/register-organisation")
    public void postMethodName(@RequestBody Organisations entity) {
        organisationsRepository.save(entity);
    }

    @GetMapping("/auth/get-organisations")
    public List<Organisations> getMethodName() {
        return organisationsRepository.findAll();
    }

}
