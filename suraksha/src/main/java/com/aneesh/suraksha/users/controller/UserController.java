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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import com.aneesh.suraksha.users.component.ClientIPAddress;
import com.aneesh.suraksha.users.controller.Login.LoginRequest;
import com.aneesh.suraksha.users.controller.Login.LoginResponse;
import com.aneesh.suraksha.users.controller.Signup.SignupRequest;
import com.aneesh.suraksha.users.controller.Signup.SignupResponse;
import com.aneesh.suraksha.users.dto.UserDTO;
import com.aneesh.suraksha.users.model.Organisations;
import com.aneesh.suraksha.users.model.OrganisationsRepository;
import com.aneesh.suraksha.users.model.UserRepository;
import com.aneesh.suraksha.users.service.RegistrationService;

import java.util.List;

@RestController
public class UserController {

    private final ClientIPAddress clientIPAddress;

    private final OrganisationsRepository organisationsRepository;

    private final LoginService loginService;

    private final UserRepository userRepository;

    private final RegistrationService registrationService;

    private final OrganisationOnboard organisationOnboard;

    public UserController(UserRepository userRepository, RegistrationService registrationService,
            LoginService loginService,
            OrganisationsRepository organisationsRepository, OrganisationOnboard organisationOnboard,
            ClientIPAddress clientIPAddress) {
        this.userRepository = userRepository;
        this.registrationService = registrationService;
        this.loginService = loginService;
        this.organisationsRepository = organisationsRepository;
        this.organisationOnboard = organisationOnboard;
        this.clientIPAddress = clientIPAddress;
    }

    @PostMapping("/api/v1/auth/token/register")
    public ResponseEntity<SignupResponse> createUser(@RequestBody SignupRequest entity, HttpServletResponse response) {
        SignupResponse res = registrationService.OnBoard(entity);
        if (res.status()) {
            Cookie cookie = new Cookie("jwt", res.token());
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60);
            response.addCookie(cookie);
        }
        return ResponseEntity.status(res.status() ? HttpStatus.OK : HttpStatus.FORBIDDEN).body(res);
    }

    @GetMapping("/api/v1/users")
    public List<UserDTO> getAllUsers() {
        return userRepository.findAllUsersAsDTO();
    }

    @PostMapping("/api/v1/auth/token/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest entity, HttpServletResponse response,
            HttpServletRequest request) {
        LoginResult res = loginService.login(entity);

        if (res.status()) {
            Cookie cookie = new Cookie("jwt", res.token());
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60);
            response.addCookie(cookie);
            String ip = clientIPAddress.getIP(request);
            String userAgent = request.getHeader("User-Agent");

        }

        return ResponseEntity.status(res.status() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED)
                .body(new LoginResponse(res.status(), res.message()));
    }

    @PostMapping("/api/v1/organisations")
    public OnboardResponse registerOrganisation(@RequestBody OnboardRequest entity) {
        OnboardResponse res = organisationOnboard.OnBoard(entity);
        return res;

    }

    @GetMapping("/api/v1/organisations")
    public List<Organisations> getMethodName() {
        return organisationsRepository.findAll();
    }

}
