package com.aneesh.suraksha.users.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.aneesh.suraksha.users.service.OtpService;

@RestController
public class TestController {

    private final OtpService otpService;

    public TestController(OtpService otpService) {
        this.otpService = otpService;
    }

    @GetMapping("/api/test/v1/start-otp-flow")
    public void testOtpFlow() {
        otpService.OtpFlow();
    }

}
