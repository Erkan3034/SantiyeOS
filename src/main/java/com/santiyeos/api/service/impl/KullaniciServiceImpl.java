package com.santiyeos.api.service.impl;

import com.santiyeos.api.dto.request.CreateKullaniciRequest;
import com.santiyeos.api.dto.request.ResetKullaniciSifreRequest;
import com.santiyeos.api.dto.request.UpdateKullaniciRequest;
import com.santiyeos.api.dto.response.KullaniciResponse;
import com.santiyeos.api.exception.BusinessException;
import com.santiyeos.api.model.Kullanici;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.repository.KullaniciRepository;
import com.santiyeos.api.security.Roles;
import com.santiyeos.api.service.KullaniciService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.santiyeos.api.service.AbonelikLimitService;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class KullaniciServiceImpl implements KullaniciService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private static final Set<String> ROLLER = Set.of(
            Roles.FIRMA_ADMIN,
            Roles.PROJE_YONETICISI,
            Roles.SAHA_PERSONELI,
            Roles.TASERON_TEMSILCI
    );

    private final KullaniciRepository kullaniciRepository;
    private final PasswordEncoder passwordEncoder;
    private final AbonelikLimitService abonelikLimitService;

    public KullaniciServiceImpl(KullaniciRepository kullaniciRepository, PasswordEncoder passwordEncoder, AbonelikLimitService abonelikLimitService) {
        this.kullaniciRepository = kullaniciRepository;
        this.passwordEncoder = passwordEncoder;
        this.abonelikLimitService = abonelikLimitService;
    }

    @Override
    public PageResult<KullaniciResponse> listele(Integer firmaId, String rol, Boolean aktif, int limit, int offset) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        String safeRol = normalizeOptionalRol(rol);
        int safeLimit = normalizeLimit(limit);
        int safeOffset = Math.max(offset, 0);

        PageResult<Kullanici> result = kullaniciRepository.listele(
                safeFirmaId,
                safeRol,
                aktif,
                safeLimit,
                safeOffset
        );

        List<KullaniciResponse> responseItems = result.getItems()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageResult<>(responseItems, result.getTotal(), result.getLimit(), result.getOffset());
    }

    @Override
    public KullaniciResponse getir(Integer firmaId, Integer kullaniciId) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeKullaniciId = validatePositive(kullaniciId, "Geçerli bir kullanıcı id giriniz.");

        Kullanici kullanici = kullaniciRepository.getir(safeFirmaId, safeKullaniciId);

        if (kullanici == null) {
            throw BusinessException.notFound("Kullanıcı bulunamadı.");
        }

        return toResponse(kullanici);
    }

    @Override
    public KullaniciResponse ekle(Integer firmaId, CreateKullaniciRequest request) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        String safeRol = normalizeRol(request.getRol());
        validateRoleAndTaseron(safeRol, request.getTaseronId());

        Kullanici kullanici = Kullanici.builder()
                .firmaId(safeFirmaId)
                .taseronId(request.getTaseronId())
                .ad(request.getAd())
                .soyad(request.getSoyad())
                .email(normalizeEmail(request.getEmail()))
                .sifreHash(passwordEncoder.encode(request.getSifre()))
                .rol(safeRol)
                .telefon(request.getTelefon())
                .build();

        abonelikLimitService.kullaniciEklemeHakkiKontrolEt(safeFirmaId);
        Integer kullaniciId = kullaniciRepository.ekle(safeFirmaId, kullanici);

        if (kullaniciId == null) {
            throw BusinessException.conflict("Kullanıcı oluşturuldu ancak id alınamadı.");
        }

        return getir(safeFirmaId, kullaniciId);
    }

    @Override
    public KullaniciResponse guncelle(Integer firmaId, Integer kullaniciId, UpdateKullaniciRequest request) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeKullaniciId = validatePositive(kullaniciId, "Geçerli bir kullanıcı id giriniz.");
        String safeRol = normalizeRol(request.getRol());
        validateRoleAndTaseron(safeRol, request.getTaseronId());

        Kullanici kullanici = Kullanici.builder()
                .taseronId(request.getTaseronId())
                .ad(request.getAd())
                .soyad(request.getSoyad())
                .email(request.getEmail())
                .telefon(request.getTelefon())
                .rol(safeRol)
                .aktif(request.getAktif())
                .build();

        Integer etkilenenSatir = kullaniciRepository.guncelle(safeFirmaId, safeKullaniciId, kullanici);

        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("Kullanıcı bulunamadı.");
        }

        return getir(safeFirmaId, safeKullaniciId);
    }

    @Override
    public void sil(Integer firmaId, Integer kullaniciId) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeKullaniciId = validatePositive(kullaniciId, "Geçerli bir kullanıcı id giriniz.");

        Integer etkilenenSatir = kullaniciRepository.sil(safeFirmaId, safeKullaniciId);

        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("Kullanıcı bulunamadı.");
        }
    }

    @Override
    public void sifreResetle(Integer kullaniciId, ResetKullaniciSifreRequest request) {
        Integer safeKullaniciId = validatePositive(kullaniciId, "Geçerli bir kullanıcı id giriniz.");

        Integer etkilenenSatir = kullaniciRepository.sifreGuncelle(
                safeKullaniciId,
                passwordEncoder.encode(request.getYeniSifre())
        );

        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("Kullanıcı bulunamadı.");
        }
    }

    private Integer validateFirmaId(Integer firmaId) {
        return validatePositive(firmaId, "Geçerli bir firma id giriniz.");
    }

    private Integer validatePositive(Integer value, String message) {
        if (value == null || value <= 0) {
            throw BusinessException.badRequest(message);
        }

        return value;
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(limit, MAX_LIMIT);
    }

    private String normalizeOptionalRol(String rol) {
        if (rol == null || rol.isBlank()) {
            return null;
        }

        return normalizeRol(rol);
    }

    private String normalizeRol(String rol) {
        if (rol == null || rol.isBlank()) {
            throw BusinessException.badRequest("Kullanıcı rolü zorunludur.");
        }

        String normalized = rol.trim().toUpperCase(Locale.ROOT);

        if (!ROLLER.contains(normalized)) {
            throw BusinessException.badRequest("Geçersiz kullanıcı rolü.");
        }

        return normalized;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void validateRoleAndTaseron(String rol, Integer taseronId) {
        if (Roles.TASERON_TEMSILCI.equals(rol) && (taseronId == null || taseronId <= 0)) {
            throw BusinessException.badRequest("Taşeron temsilcisi için taşeron zorunludur.");
        }

        if (!Roles.TASERON_TEMSILCI.equals(rol) && taseronId != null) {
            throw BusinessException.badRequest("Sadece taşeron temsilcisi taşerona bağlanabilir.");
        }
    }

    private KullaniciResponse toResponse(Kullanici kullanici) {
        return KullaniciResponse.builder()
                .kullaniciId(kullanici.getKullaniciId())
                .firmaId(kullanici.getFirmaId())
                .taseronId(kullanici.getTaseronId())
                .ad(kullanici.getAd())
                .soyad(kullanici.getSoyad())
                .email(kullanici.getEmail())
                .rol(kullanici.getRol())
                .telefon(kullanici.getTelefon())
                .aktif(kullanici.getAktif())
                .sonGiris(kullanici.getSonGiris())
                .createdAt(kullanici.getCreatedAt())
                .updatedAt(kullanici.getUpdatedAt())
                .firmaAd(kullanici.getFirmaAd())
                .taseronAd(kullanici.getTaseronAd())
                .build();
    }
}