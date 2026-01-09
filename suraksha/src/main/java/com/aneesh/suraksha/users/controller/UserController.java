package com.aneesh.suraksha.users.controller;

import com.aneesh.suraksha.users.service.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import com.aneesh.suraksha.users.component.ClientIPAddress;
import com.aneesh.suraksha.users.dto.AuthResult;
import com.aneesh.suraksha.users.dto.CreateRefreshTokenRequest;
import com.aneesh.suraksha.users.dto.LoginRequest;
import com.aneesh.suraksha.users.dto.LoginResponse;
import com.aneesh.suraksha.users.dto.CreateOrganizationRequest;
import com.aneesh.suraksha.users.dto.CreateOrganizationResponse;
import com.aneesh.suraksha.users.dto.RegisterRequest;
import com.aneesh.suraksha.users.dto.RegisterResponse;
import com.aneesh.suraksha.users.dto.RegisterResult;
import com.aneesh.suraksha.users.dto.MagicLinkRequest;
import com.aneesh.suraksha.users.dto.MagicLinkResponse;
import com.aneesh.suraksha.users.dto.MagicLinkVerifyRequest;

import com.aneesh.suraksha.users.dto.OTPRequest;
import com.aneesh.suraksha.users.dto.OTPResponse;
import com.aneesh.suraksha.users.dto.RefreshCheckCheckResponse;
import com.aneesh.suraksha.users.dto.RefreshResponse;
import com.aneesh.suraksha.users.dto.UserDto;
import com.aneesh.suraksha.users.dto.RequestMetadata;
import com.aneesh.suraksha.users.dto.ResetPasswordRequest;
import com.aneesh.suraksha.users.dto.ResetPasswordResponse;
import com.aneesh.suraksha.users.dto.TokenSubject;
import com.aneesh.suraksha.users.dto.MagicLinkResult;
import com.aneesh.suraksha.users.dto.LogoutResponse;
import com.aneesh.suraksha.users.model.Organisations;
import com.aneesh.suraksha.users.model.OrganisationsRepository;
import com.aneesh.suraksha.users.model.UserEntity;
import com.aneesh.suraksha.users.model.UserRepository;

import java.util.List;

@RestController
public class UserController {

    private final TwofactorService twofactorService;

    private final ValidRedirectService validRedirectService;

    private final RefreshCheck refreshCheck;

    private final MagicUrlService magicUrlService;

    private final ClientIPAddress clientIPAddress;

    private final OrganisationsRepository organisationsRepository;

    private final LoginService loginService;

    private final UserRepository userRepository;

    private final RegistrationService registrationService;

    private final OrganisationOnboard organisationOnboard;

    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    private final LogoutService logoutService;

    public UserController(UserRepository userRepository, RegistrationService registrationService,
            LoginService loginService,
            OrganisationsRepository organisationsRepository, OrganisationOnboard organisationOnboard,
            ClientIPAddress clientIPAddress,
            RefreshTokenService refreshTokenService, MagicUrlService magicUrlService, RefreshCheck refreshCheck,
            JwtService jwtService, LogoutService logoutService, ValidRedirectService validRedirectService,
            TwofactorService twofactorService) {
        this.userRepository = userRepository;
        this.registrationService = registrationService;
        this.loginService = loginService;
        this.organisationsRepository = organisationsRepository;
        this.organisationOnboard = organisationOnboard;
        this.clientIPAddress = clientIPAddress;
        this.magicUrlService = magicUrlService;
        this.refreshCheck = refreshCheck;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.logoutService = logoutService;
        this.validRedirectService = validRedirectService;
        this.twofactorService = twofactorService;
    }

    @PostMapping("/api/v1/auth/register")
    public ResponseEntity<RegisterResponse> createUser(@RequestBody RegisterRequest entity,
            HttpServletResponse response,
            HttpServletRequest request) {
        String ip = clientIPAddress.getIP(request);
        String userAgent = request.getHeader("User-Agent");
        RequestMetadata metaData = new RequestMetadata(ip, userAgent);
        RegisterResult res = registrationService.OnBoard(entity, metaData);
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
        return ResponseEntity.status(res.status() ? HttpStatus.OK : HttpStatus.FORBIDDEN)
                .body(new RegisterResponse(res.status(), res.message(), res.token(), res.refreshToken()));
    }

    @PostMapping("/api/v1/auth/logout")
    public ResponseEntity<LogoutResponse> logout(
            @CookieValue(name = "refresh_token", required = false) String rawToken) {
        if (rawToken == null || rawToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LogoutResponse(false, "No refresh token provided"));
        }
        Boolean success = logoutService.logout(rawToken);
        if (success) {
            return ResponseEntity.ok(new LogoutResponse(true, "Logged out successfully"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new LogoutResponse(false, "Failed to logout"));
    }

    @GetMapping("/api/v1/users")
    public List<UserDto> getAllUsers() {
        return userRepository.findAllUsersAsDto();
    }

    @PostMapping("/api/v1/auth/refresh")
    public ResponseEntity<RefreshResponse> refresh(
            @CookieValue(name = "refresh_token", required = false) String rawToken,
            HttpServletResponse response) {
        if (rawToken == null || rawToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        RefreshCheckCheckResponse status = refreshCheck.Check(rawToken);
        if (!status.status()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = jwtService.generateToken(status.subject());
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(15 * 60)
                .build();
        response.addHeader("Set-Cookie", jwtCookie.toString());
        return ResponseEntity.ok(new RefreshResponse(token));
    }

    @PostMapping("/api/v1/auth/2fa/otp")
    public ResponseEntity<OTPResponse> OTPVerify(@RequestBody OTPRequest entity, HttpServletResponse response,
            HttpServletRequest request, @RequestParam(required = false) String redirect) {
        TokenSubject token = twofactorService.Validate(entity.OTP());
        if (token == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String ip = clientIPAddress.getIP(request);
        String userAgent = request.getHeader("User-Agent");
        String jwt = jwtService.generateToken(token);
        String refreshToken = refreshTokenService.generate(
                new CreateRefreshTokenRequest(token, ip, userAgent));

        ResponseCookie jwtCookie = ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(1 * 60)
                .build();
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(30 * 24 * 60 * 60)
                .build();
        String redirectUrl = (redirect != null && !redirect.isBlank()) ? redirect : "/";
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString(), refreshCookie.toString())
                .header(HttpHeaders.LOCATION, redirectUrl)
                .build();

    }

    @PostMapping("/api/v1/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest entity,
            @RequestParam(required = false) String redirect, HttpServletResponse response,
            HttpServletRequest request) {
        Boolean isRedirectAllowed = validRedirectService.validate(entity.organisationId(), redirect);
        if (!isRedirectAllowed) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        AuthResult authResult = loginService.authenticate(entity);
        if (authResult.status().equals("2fa")) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(new LoginResponse("2fa", "Two-factor authentication required", authResult.token()));
        }
        if (!authResult.status().equals("true")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Generate tokens
        String ip = clientIPAddress.getIP(request);
        String userAgent = request.getHeader("User-Agent");
        String jwt = jwtService.generateToken(authResult.subject());
        String refreshToken = refreshTokenService.generate(
                new CreateRefreshTokenRequest(authResult.subject(), ip, userAgent));

        ResponseCookie jwtCookie = ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(1 * 60)
                .build();
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(30 * 24 * 60 * 60)
                .build();
        String redirectUrl = (redirect != null && !redirect.isBlank()) ? redirect : "/";
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString(), refreshCookie.toString())
                .header(HttpHeaders.LOCATION, redirectUrl)
                .build();
    }

    @PostMapping("/api/v1/organisations")
    public CreateOrganizationResponse registerOrganisation(@RequestBody CreateOrganizationRequest entity) {
        CreateOrganizationResponse res = organisationOnboard.OnBoard(entity);
        return res;

    }

    @PostMapping("/api/v1/auth/forgot-password")
    public ResponseEntity<ResetPasswordResponse> ForgotPassword(@RequestBody ResetPasswordRequest entity) {
        UserEntity user = userRepository.findByMailId(entity.mailId());

    }

    @PostMapping("/api/v1/magic-url")
    public ResponseEntity<MagicLinkResponse> magicURL(@RequestBody MagicLinkRequest entity,
            @RequestParam(required = false) String redirect) {
        UserEntity user = userRepository.findByMailId(entity.mailId());
        if (user != null) {
            magicUrlService.SendMagicUrl(user, redirect);
        }
        return ResponseEntity.ok(new MagicLinkResponse(true));
    }

    @GetMapping("/api/v1/verify-magic-url")
    public ResponseEntity<Void> verifyMagicURL(@ModelAttribute MagicLinkVerifyRequest param,
            HttpServletRequest request, HttpServletResponse response) {
        String ip = clientIPAddress.getIP(request);
        String userAgent = request.getHeader("User-Agent");
        RequestMetadata metaData = new RequestMetadata(ip, userAgent);

        Boolean isConditionMet = validRedirectService.validate("magic-link", param.redirect());
        if (!isConditionMet) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        MagicLinkResult res = magicUrlService.verifySendMagicUrl(param.token(), metaData);
        if (!res.status()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ResponseCookie jwtCookie = ResponseCookie.from("jwt", res.jwt())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(1 * 60)
                .build();

        ResponseCookie refreshToken = ResponseCookie.from("refresh_token", res.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(30 * 24 * 60 * 60)
                .build();

        String redirectUrl = (param.redirect() != null && !param.redirect().isBlank()) ? param.redirect() : "/";

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString(), refreshToken.toString())
                .header(HttpHeaders.LOCATION, redirectUrl)
                .build();
    }

    @GetMapping("/api/v1/organisations")
    public List<Organisations> getMethodName() {
        return organisationsRepository.findAll();
    }

}
