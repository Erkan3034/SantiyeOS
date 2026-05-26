package com.santiyeos.api.service;

import com.santiyeos.api.dto.request.LoginRequest;
import com.santiyeos.api.dto.request.ChangePasswordRequest;
import com.santiyeos.api.dto.response.AuthKullaniciResponse;
import com.santiyeos.api.dto.response.AuthResponse;
import com.santiyeos.api.security.CurrentUser;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthKullaniciResponse me(CurrentUser currentUser);

    AuthKullaniciResponse sifreDegistir(CurrentUser currentUser, ChangePasswordRequest request);
}
