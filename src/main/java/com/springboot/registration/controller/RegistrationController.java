package com.springboot.registration.controller;

import com.springboot.registration.service.CodeAuthService;
import com.springboot.registration.service.RegistrationAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/identity")
public class RegistrationController {
    @Autowired
    private CodeAuthService authService;

    @Autowired
    private RegistrationAuthService registrationService;

    @PostMapping("/verify")
    public Map<String, Object> verifyIdentity(@RequestParam String name,
                                              @RequestParam String phoneNo,
                                              @RequestParam String identity,
                                              @RequestParam int telecom) {
        String accessToken = authService.getAccessToken();
        return registrationService.verifyIdentity(accessToken, identity, name, phoneNo, telecom);
    }

    @PostMapping("/add-verify")
    public Map<String, Object> completeAuth(@RequestParam String name,
                                            @RequestParam String phoneNo,
                                            @RequestParam String identity,
                                            @RequestParam int telecom) {
        String accessToken = authService.getAccessToken();
        return registrationService.addVerify(accessToken, identity, name, phoneNo, telecom);
    }
}
