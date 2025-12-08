package com.aneesh.suraksha.users.controller;

import com.aneesh.suraksha.users.service.LoginResult;
import com.aneesh.suraksha.users.service.LoginService;
import com.aneesh.suraksha.users.service.OnboardRequest;
import com.aneesh.suraksha.users.service.OnboardResponse;
import com.aneesh.suraksha.users.service.OrganisationOnboard;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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

@RestController
public class UserController {

    private final OrganisationsRepository organisationsRepository;

    private final LoginService loginService;

    private final UserRepository userRepository;

    private final HashService hashService;

    private final OrganisationOnboard organisationOnboard;

    public UserController(UserRepository userRepository, HashService hashService, LoginService loginService,
            OrganisationsRepository organisationsRepository, OrganisationOnboard organisationOnboard) {
        this.userRepository = userRepository;
        this.hashService = hashService;
        this.loginService = loginService;
        this.organisationsRepository = organisationsRepository;
        this.organisationOnboard = organisationOnboard;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> createUser(@RequestBody SignupRequest entity, HttpServletResponse response) {
        SignupResponse res = hashService.OnBoard(entity, response);
        return ResponseEntity.status(res.status() ? HttpStatus.OK : HttpStatus.FORBIDDEN).body(res);
    }

    @GetMapping("/users")
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest entity, HttpServletResponse response) {
        LoginResult res = loginService.login(entity);
        if (res.status()) {
            Cookie cookie = new Cookie("jwt", res.token());
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60);
            response.addCookie(cookie);
        }
        return ResponseEntity.status(res.status() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED)
                .body(new LoginResponse(res.status(), res.message()));
    }

    @PostMapping("/auth/register-organisation")
    public OnboardResponse registerOrganisation(@RequestBody OnboardRequest entity) {
        OnboardResponse res = organisationOnboard.OnBoard(entity);
        return res;

    }

    @GetMapping("/auth/get-organisations")
    public List<Organisations> getMethodName() {
        return organisationsRepository.findAll();
    }

}
