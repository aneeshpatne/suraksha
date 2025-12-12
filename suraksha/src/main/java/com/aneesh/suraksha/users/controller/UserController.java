package com.aneesh.suraksha.users.controller;

import com.aneesh.suraksha.users.service.*;

import org.flywaydb.core.internal.nc.MetaData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
import com.aneesh.suraksha.users.controller.Signup.SignupResult;
import com.aneesh.suraksha.users.dto.SendMagicLinkRequest;
import com.aneesh.suraksha.users.dto.SendMagicLinkResponse;
import com.aneesh.suraksha.users.dto.VerifyMagicLinkRequest;
import com.aneesh.suraksha.users.dto.VerifyMagicLinkResponse;
import com.aneesh.suraksha.users.dto.UserDTO;
import com.aneesh.suraksha.users.dto.UserMetaData;
import com.aneesh.suraksha.users.dto.MagicLinkVerificationResult;
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
    public ResponseEntity<SignupResponse> createUser(@RequestBody SignupRequest entity, HttpServletResponse response,
            HttpServletRequest request) {
        String ip = clientIPAddress.getIP(request);
        String userAgent = request.getHeader("User-Agent");
        UserMetaData metaData = new UserMetaData(ip, userAgent);
        SignupResult res = registrationService.OnBoard(entity, metaData);
        return ResponseEntity.status(res.status() ? HttpStatus.OK : HttpStatus.FORBIDDEN)
                .body(new SignupResponse(res.status(), res.message(), res.token(), res.refreshToken()));
    }

    @GetMapping("/api/v1/users")
    public List<UserDTO> getAllUsers() {
        return userRepository.findAllUsersAsDTO();
    }

    @PostMapping("/api/v1/auth/token/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest entity, HttpServletResponse response,
            HttpServletRequest request) {
        String ip = clientIPAddress.getIP(request);
        String userAgent = request.getHeader("User-Agent");
        UserMetaData metaData = new UserMetaData(ip, userAgent);
        LoginResult res = loginService.login(entity, metaData);

        return ResponseEntity.status(res.status() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED)
                .body(new LoginResponse(res.status(), res.message(), res.token(), res.refreshToken()));
    }

    @PostMapping("/api/v1/organisations")
    public OnboardResponse registerOrganisation(@RequestBody OnboardRequest entity) {
        OnboardResponse res = organisationOnboard.OnBoard(entity);
        return res;

    }

    @PostMapping("/api/v1/magic-url")
    public SendMagicLinkResponse magicURL(@RequestBody SendMagicLinkRequest entity) {
        UserEntity user = userRepository.findByMailId(entity.mailId());
        magicUrlService.SendMagicUrl(user);
        SendMagicLinkResponse res = new SendMagicLinkResponse(true);
        return res;

    }

    @GetMapping("/api/v1/verify-magic-url")
    public ResponseEntity<VerifyMagicLinkResponse> verifyMagicURL(@ModelAttribute VerifyMagicLinkRequest param) {
        MagicLinkVerificationResult res = magicUrlService.verifySendMagicUrl(param.token());
        if (!res.status()) {
            return ResponseEntity.badRequest().body(new VerifyMagicLinkResponse(null, false));
        }
        return ResponseEntity.ok().body(new VerifyMagicLinkResponse(res.userId(), true));
    }

    @GetMapping("/api/v1/organisations")
    public List<Organisations> getMethodName() {
        return organisationsRepository.findAll();
    }

}
