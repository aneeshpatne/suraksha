package com.aneesh.suraksha.users.controller;

import com.aneesh.suraksha.users.service.*;
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
import com.aneesh.suraksha.users.dto.MagicPostRequestDTO;
import com.aneesh.suraksha.users.dto.MagicPostResponseDTO;
import com.aneesh.suraksha.users.dto.UserDTO;
import com.aneesh.suraksha.users.model.Organisations;
import com.aneesh.suraksha.users.model.OrganisationsRepository;
import com.aneesh.suraksha.users.model.UserEntity;
import com.aneesh.suraksha.users.model.UserRepository;
import com.aneesh.suraksha.users.service.RegistrationService;

import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class UserController {

    private final MagicUrlService magicUrlService;

    private final ClientIPAddress clientIPAddress;

    private final OrganisationsRepository organisationsRepository;

    private final LoginService loginService;

    private final UserRepository userRepository;

    private final RegistrationService registrationService;

    private final OrganisationOnboard organisationOnboard;

    private final RefreshTokenService refreshTokenService;

    public UserController(UserRepository userRepository, RegistrationService registrationService,
            LoginService loginService,
            OrganisationsRepository organisationsRepository, OrganisationOnboard organisationOnboard,
            ClientIPAddress clientIPAddress,
            RefreshTokenService refreshTokenService, MagicUrlService magicUrlService) {
        this.userRepository = userRepository;
        this.registrationService = registrationService;
        this.loginService = loginService;
        this.organisationsRepository = organisationsRepository;
        this.organisationOnboard = organisationOnboard;
        this.clientIPAddress = clientIPAddress;
        this.refreshTokenService = refreshTokenService;
        this.magicUrlService = magicUrlService;
    }

    @PostMapping("/api/v1/auth/token/register")
    public ResponseEntity<SignupResponse> createUser(@RequestBody SignupRequest entity, HttpServletResponse response,
            HttpServletRequest request) {
        SignupResponse res = registrationService.OnBoard(entity);
        if (res.status()) {
            Cookie cookie = new Cookie("jwt", res.token());
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60);
            response.addCookie(cookie);

            String ip = clientIPAddress.getIP(request);
            String userAgent = request.getHeader("User-Agent");

            RefreshTokenServiceRequest tokenReq = new RefreshTokenServiceRequest(res.user(), ip, userAgent);
            RefreshTokenServiceResponse tokenRes = refreshTokenService.generate(tokenReq);

            Cookie refreshCookie = new Cookie("refresh_token", tokenRes.token());
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(true);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(60 * 60 * 24 * 7); // 7 days
            response.addCookie(refreshCookie);

            Cookie refreshIdCookie = new Cookie("refresh_token_id", tokenRes.id().toString());
            refreshIdCookie.setHttpOnly(true);
            refreshIdCookie.setSecure(true);
            refreshIdCookie.setPath("/");
            refreshIdCookie.setMaxAge(60 * 60 * 24 * 7); // 7 days
            response.addCookie(refreshIdCookie);
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

            RefreshTokenServiceRequest tokenReq = new RefreshTokenServiceRequest(
                    res.user(), ip, userAgent);
            RefreshTokenServiceResponse tokenRes = refreshTokenService.generate(tokenReq);

            Cookie refreshCookie = new Cookie("refresh_token", tokenRes.token());
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(true);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(60 * 60 * 24 * 7); // 7 days
            response.addCookie(refreshCookie);

            Cookie refreshIdCookie = new Cookie("refresh_token_id", tokenRes.id().toString());
            refreshIdCookie.setHttpOnly(true);
            refreshIdCookie.setSecure(true);
            refreshIdCookie.setPath("/");
            refreshIdCookie.setMaxAge(60 * 60 * 24 * 7); // 7 days
            response.addCookie(refreshIdCookie);
        }

        return ResponseEntity.status(res.status() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED)
                .body(new LoginResponse(res.status(), res.message()));
    }

    @PostMapping("/api/v1/organisations")
    public OnboardResponse registerOrganisation(@RequestBody OnboardRequest entity) {
        OnboardResponse res = organisationOnboard.OnBoard(entity);
        return res;

    }

    @PostMapping("/api/v1/magic-url")
    public MagicPostResponseDTO magicURL(@RequestBody MagicPostRequestDTO entity) {
        UserEntity user = userRepository.findByMailId(entity.mailId());
        magicUrlService.SendMagicUrl(user);
        MagicPostResponseDTO res = new MagicPostResponseDTO(true);
        return res;

    }

    @GetMapping("/api/v1/verify-magic-url")
    public String verifyMagicURL(@RequestParam String param) {
        return new String();
    }

    @GetMapping("/api/v1/organisations")
    public List<Organisations> getMethodName() {
        return organisationsRepository.findAll();
    }

}
