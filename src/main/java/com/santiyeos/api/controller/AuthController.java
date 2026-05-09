package com.santiyeos.api.controller;

import com.santiyeos.api.dto.request.LoginRequest;
import com.santiyeos.api.dto.response.AuthKullaniciResponse;
import com.santiyeos.api.dto.response.AuthResponse;
import com.santiyeos.api.security.CurrentUser;
import com.santiyeos.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthKullaniciResponse me(@AuthenticationPrincipal CurrentUser currentUser) {
        return authService.me(currentUser);
    }
}
