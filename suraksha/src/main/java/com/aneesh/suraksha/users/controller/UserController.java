package com.aneesh.suraksha.users.controller;

import com.aneesh.suraksha.users.service.*;

import jakarta.servlet.http.Cookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import com.aneesh.suraksha.users.component.ClientIPAddress;
import com.aneesh.suraksha.users.dto.LoginRequest;
import com.aneesh.suraksha.users.dto.LoginResponse;
import com.aneesh.suraksha.users.dto.LoginResult;
import com.aneesh.suraksha.users.dto.CreateOrganizationRequest;
import com.aneesh.suraksha.users.dto.CreateOrganizationResponse;
import com.aneesh.suraksha.users.dto.RegisterRequest;
import com.aneesh.suraksha.users.dto.RegisterResponse;
import com.aneesh.suraksha.users.dto.RegisterResult;
import com.aneesh.suraksha.users.dto.MagicLinkRequest;
import com.aneesh.suraksha.users.dto.MagicLinkResponse;
import com.aneesh.suraksha.users.dto.MagicLinkVerifyRequest;
import com.aneesh.suraksha.users.dto.MagicLinkVerifyResponse;
import com.aneesh.suraksha.users.dto.UserDto;
import com.aneesh.suraksha.users.dto.RequestMetadata;
import com.aneesh.suraksha.users.dto.MagicLinkResult;
import com.aneesh.suraksha.users.model.Organisations;
import com.aneesh.suraksha.users.model.OrganisationsRepository;
import com.aneesh.suraksha.users.model.UserEntity;
import com.aneesh.suraksha.users.model.UserRepository;

import java.util.List;

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
    public ResponseEntity<RegisterResponse> createUser(@RequestBody RegisterRequest entity,
            HttpServletResponse response,
            HttpServletRequest request) {
        String ip = clientIPAddress.getIP(request);
        String userAgent = request.getHeader("User-Agent");
        RequestMetadata metaData = new RequestMetadata(ip, userAgent);
        RegisterResult res = registrationService.OnBoard(entity, metaData);
        return ResponseEntity.status(res.status() ? HttpStatus.OK : HttpStatus.FORBIDDEN)
                .body(new RegisterResponse(res.status(), res.message(), res.token(), res.refreshToken()));
    }

    @GetMapping("/api/v1/users")
    public List<UserDto> getAllUsers() {
        return userRepository.findAllUsersAsDto();
    }

    @PostMapping("/api/v1/auth/token/refresh")
    public String postMethodName(@RequestBody String entity) {
        // TODO: process POST request

        return entity;
    }

    @PostMapping("/api/v1/auth/token/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest entity, HttpServletResponse response,
            HttpServletRequest request) {
        String ip = clientIPAddress.getIP(request);
        String userAgent = request.getHeader("User-Agent");
        RequestMetadata metaData = new RequestMetadata(ip, userAgent);
        LoginResult res = loginService.login(entity, metaData);
        if (res.status()) {
            ResponseCookie refreshToken = ResponseCookie.from("refresh_token", res.refreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .sameSite("Strict")
                    .maxAge(30 * 24 * 60 * 60)
                    .build();
            response.addHeader("Set-Cookie", refreshToken.toString());
        }
        return ResponseEntity.status(res.status() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED)
                .body(new LoginResponse(res.status(), res.message(), res.token()));
    }

    @PostMapping("/api/v1/organisations")
    public CreateOrganizationResponse registerOrganisation(@RequestBody CreateOrganizationRequest entity) {
        CreateOrganizationResponse res = organisationOnboard.OnBoard(entity);
        return res;

    }

    @PostMapping("/api/v1/magic-url")
    public MagicLinkResponse magicURL(@RequestBody MagicLinkRequest entity) {
        UserEntity user = userRepository.findByMailId(entity.mailId());
        magicUrlService.SendMagicUrl(user);
        MagicLinkResponse res = new MagicLinkResponse(true);
        return res;

    }

    @GetMapping("/api/v1/verify-magic-url")
    public ResponseEntity<MagicLinkVerifyResponse> verifyMagicURL(@ModelAttribute MagicLinkVerifyRequest param) {
        MagicLinkResult res = magicUrlService.verifySendMagicUrl(param.token());
        if (!res.status()) {
            return ResponseEntity.badRequest().body(new MagicLinkVerifyResponse(null, false));
        }
        return ResponseEntity.ok().body(new MagicLinkVerifyResponse(res.userId(), true));
    }

    @GetMapping("/api/v1/organisations")
    public List<Organisations> getMethodName() {
        return organisationsRepository.findAll();
    }

}
