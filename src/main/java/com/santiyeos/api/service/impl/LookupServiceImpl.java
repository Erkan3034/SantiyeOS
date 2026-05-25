package com.santiyeos.api.service.impl;

import com.santiyeos.api.dto.response.LookupResponse;
import com.santiyeos.api.exception.BusinessException;
import com.santiyeos.api.model.LookupItem;
import com.santiyeos.api.repository.LookupRepository;
import com.santiyeos.api.security.Roles;
import com.santiyeos.api.service.LookupService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class LookupServiceImpl implements LookupService {

    private static final Set<String> KULLANICI_ROLLERI = Set.of(
            Roles.FIRMA_ADMIN,
            Roles.PROJE_YONETICISI,
            Roles.SAHA_PERSONELI,
            Roles.TASERON_TEMSILCI
    );

    private final LookupRepository lookupRepository;

    public LookupServiceImpl(LookupRepository lookupRepository) {
        this.lookupRepository = lookupRepository;
    }

    @Override
    public List<LookupResponse.Proje> projeler(Integer firmaId, Integer kullaniciId, String rol) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeKullaniciId = validatePositive(kullaniciId, "Gecerli bir kullanici id giriniz.");
        String safeRol = normalizeCurrentRole(rol);

        return lookupRepository.projeler(safeFirmaId, safeKullaniciId, safeRol)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<LookupResponse.Taseron> taseronlar(Integer firmaId) {
        Integer safeFirmaId = validateFirmaId(firmaId);

        return lookupRepository.taseronlar(safeFirmaId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<LookupResponse.Kullanici> kullanicilar(Integer firmaId, String rol) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        String safeRol = normalizeOptionalKullaniciRol(rol);

        return lookupRepository.kullanicilar(safeFirmaId, safeRol)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<LookupResponse.Malzeme> malzemeler(Integer firmaId) {
        Integer safeFirmaId = validateFirmaId(firmaId);

        return lookupRepository.malzemeler(safeFirmaId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<LookupResponse.AbonelikPlan> abonelikPlanlari() {
        return lookupRepository.abonelikPlanlari()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Integer validateFirmaId(Integer firmaId) {
        return validatePositive(firmaId, "Gecerli bir firma id giriniz.");
    }

    private Integer validatePositive(Integer value, String message) {
        if (value == null || value <= 0) {
            throw BusinessException.badRequest(message);
        }

        return value;
    }

    private String normalizeCurrentRole(String rol) {
        if (rol == null || rol.isBlank()) {
            throw BusinessException.badRequest("Gecerli bir kullanici rolu bulunamadi.");
        }

        return rol.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeOptionalKullaniciRol(String rol) {
        if (rol == null || rol.isBlank()) {
            return null;
        }

        String normalized = rol.trim().toUpperCase(Locale.ROOT);
        if (!KULLANICI_ROLLERI.contains(normalized)) {
            throw BusinessException.badRequest("Gecersiz kullanici rolu.");
        }

        return normalized;
    }

    private LookupResponse.Proje toResponse(LookupItem.Proje proje) {
        return LookupResponse.Proje.builder()
                .projeId(proje.getProjeId())
                .ad(proje.getAd())
                .durum(proje.getDurum())
                .build();
    }

    private LookupResponse.Taseron toResponse(LookupItem.Taseron taseron) {
        return LookupResponse.Taseron.builder()
                .taseronId(taseron.getTaseronId())
                .ad(taseron.getAd())
                .uzmanlik(taseron.getUzmanlik())
                .build();
    }

    private LookupResponse.Kullanici toResponse(LookupItem.Kullanici kullanici) {
        return LookupResponse.Kullanici.builder()
                .kullaniciId(kullanici.getKullaniciId())
                .taseronId(kullanici.getTaseronId())
                .ad(kullanici.getAd())
                .soyad(kullanici.getSoyad())
                .email(kullanici.getEmail())
                .rol(kullanici.getRol())
                .taseronAd(kullanici.getTaseronAd())
                .build();
    }

    private LookupResponse.Malzeme toResponse(LookupItem.Malzeme malzeme) {
        return LookupResponse.Malzeme.builder()
                .malzemeId(malzeme.getMalzemeId())
                .kategoriId(malzeme.getKategoriId())
                .ad(malzeme.getAd())
                .birim(malzeme.getBirim())
                .kategoriAd(malzeme.getKategoriAd())
                .stokMiktari(malzeme.getStokMiktari())
                .build();
    }

    private LookupResponse.AbonelikPlan toResponse(LookupItem.AbonelikPlan plan) {
        return LookupResponse.AbonelikPlan.builder()
                .planId(plan.getPlanId())
                .ad(plan.getAd())
                .maxProje(plan.getMaxProje())
                .maxKullanici(plan.getMaxKullanici())
                .maxTaseron(plan.getMaxTaseron())
                .aylikUcret(plan.getAylikUcret())
                .build();
    }
}
