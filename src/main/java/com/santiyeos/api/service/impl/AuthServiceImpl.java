package com.santiyeos.api.service.impl;

import com.santiyeos.api.dto.request.ChangePasswordRequest;
import com.santiyeos.api.dto.request.LoginRequest;
import com.santiyeos.api.dto.response.AuthKullaniciResponse;
import com.santiyeos.api.dto.response.AuthResponse;
import com.santiyeos.api.exception.BusinessException;
import com.santiyeos.api.model.Kullanici;
import com.santiyeos.api.repository.AuthRepository;
import com.santiyeos.api.repository.KullaniciRepository;
import com.santiyeos.api.security.CurrentUser;
import com.santiyeos.api.security.JwtService;
import com.santiyeos.api.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;
    private final KullaniciRepository kullaniciRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(
            AuthRepository authRepository,
            KullaniciRepository kullaniciRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.authRepository = authRepository;
        this.kullaniciRepository = kullaniciRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        Kullanici kullanici = authRepository.emailIleGetir(email);

        if (kullanici == null || !passwordEncoder.matches(request.getSifre(), kullanici.getSifreHash())) {
            throw BusinessException.unauthorized("E-posta veya şifre hatalı.");
        }

        authRepository.sonGirisGuncelle(kullanici.getKullaniciId());

        String accessToken = jwtService.createAccessToken(kullanici);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresInSeconds(jwtService.getExpirationSeconds())
                .kullanici(toAuthKullaniciResponse(kullanici))
                .build();
    }

    @Override
    public AuthKullaniciResponse me(CurrentUser currentUser) {
        if (currentUser == null || currentUser.getKullaniciId() == null) {
            throw BusinessException.unauthorized("Geçerli kullanıcı bulunamadı.");
        }

        Kullanici kullanici = authRepository.idIleGetir(currentUser.getKullaniciId());

        if (kullanici == null) {
            throw BusinessException.unauthorized("Kullanıcı pasif veya bulunamadı.");
        }

        return toAuthKullaniciResponse(kullanici);
    }

    @Override
    public AuthKullaniciResponse sifreDegistir(CurrentUser currentUser, ChangePasswordRequest request) {
        if (currentUser == null || currentUser.getKullaniciId() == null) {
            throw BusinessException.unauthorized("Gecerli kullanici bulunamadi.");
        }

        Kullanici kullanici = authRepository.idIleGetir(currentUser.getKullaniciId());
        if (kullanici == null) {
            throw BusinessException.unauthorized("Kullanici pasif veya bulunamadi.");
        }

        if (!passwordEncoder.matches(request.getMevcutSifre(), kullanici.getSifreHash())) {
            throw BusinessException.badRequest("Mevcut sifre hatali.");
        }

        if (passwordEncoder.matches(request.getYeniSifre(), kullanici.getSifreHash())) {
            throw BusinessException.badRequest("Yeni sifre mevcut sifreden farkli olmalidir.");
        }

        Integer etkilenenSatir = kullaniciRepository.sifreGuncelle(
                kullanici.getKullaniciId(),
                passwordEncoder.encode(request.getYeniSifre()),
                false
        );

        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("Sifre guncellenemedi.");
        }

        Kullanici guncelKullanici = authRepository.idIleGetir(kullanici.getKullaniciId());
        return toAuthKullaniciResponse(guncelKullanici);
    }

    private AuthKullaniciResponse toAuthKullaniciResponse(Kullanici kullanici) {
        return AuthKullaniciResponse.builder()
                .kullaniciId(kullanici.getKullaniciId())
                .firmaId(kullanici.getFirmaId())
                .taseronId(kullanici.getTaseronId())
                .ad(kullanici.getAd())
                .soyad(kullanici.getSoyad())
                .email(kullanici.getEmail())
                .rol(kullanici.getRol())
                .telefon(kullanici.getTelefon())
                .sifreDegistirmeli(kullanici.getSifreDegistirmeli())
                .build();
    }
}
