package com.santiyeos.api.service.impl;

import com.santiyeos.api.dto.response.ProjeKullaniciResponse;
import com.santiyeos.api.exception.BusinessException;
import com.santiyeos.api.model.Kullanici;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.model.ProjeKullanici;
import com.santiyeos.api.repository.KullaniciRepository;
import com.santiyeos.api.repository.ProjeKullaniciRepository;
import com.santiyeos.api.security.Roles;
import com.santiyeos.api.service.ProjeKullaniciService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjeKullaniciServiceImpl implements ProjeKullaniciService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final ProjeKullaniciRepository projeKullaniciRepository;
    private final KullaniciRepository kullaniciRepository;

    public ProjeKullaniciServiceImpl(ProjeKullaniciRepository projeKullaniciRepository, KullaniciRepository kullaniciRepository) {
        this.projeKullaniciRepository = projeKullaniciRepository;
        this.kullaniciRepository = kullaniciRepository;
    }

    @Override
    public void ata(Integer firmaId, Integer projeId, Integer kullaniciId) {
        Integer safeFirmaId = validatePositive(firmaId, "Gecerli bir firma id giriniz.");
        Integer safeProjeId = validatePositive(projeId, "Gecerli bir proje id giriniz.");
        Integer safeKullaniciId = validatePositive(kullaniciId, "Gecerli bir kullanici id giriniz.");
        Kullanici kullanici = kullaniciRepository.getir(safeFirmaId, safeKullaniciId);

        if (kullanici == null || !safeFirmaId.equals(kullanici.getFirmaId())) {
            throw BusinessException.notFound("Kullanici bulunamadi.");
        }

        if (!Roles.PROJE_YONETICISI.equals(kullanici.getRol())) {
            throw BusinessException.badRequest("Sadece proje yoneticisi projeye atanabilir.");
        }

        if (Boolean.FALSE.equals(kullanici.getAktif())) {
            throw BusinessException.badRequest("Pasif kullanici projeye atanamaz.");
        }

        projeKullaniciRepository.ata(safeFirmaId, safeProjeId, safeKullaniciId);
    }

    @Override
    public void kaldir(Integer firmaId, Integer projeId, Integer kullaniciId) {
        projeKullaniciRepository.kaldir(
                validatePositive(firmaId, "Gecerli bir firma id giriniz."),
                validatePositive(projeId, "Gecerli bir proje id giriniz."),
                validatePositive(kullaniciId, "Gecerli bir kullanici id giriniz.")
        );
    }

    @Override
    public PageResult<ProjeKullaniciResponse> listele(Integer firmaId, Integer projeId, int limit, int offset) {
        Integer safeFirmaId = validatePositive(firmaId, "Gecerli bir firma id giriniz.");
        Integer safeProjeId = validatePositive(projeId, "Gecerli bir proje id giriniz.");
        int safeLimit = normalizeLimit(limit);
        int safeOffset = Math.max(offset, 0);
        PageResult<ProjeKullanici> result = projeKullaniciRepository.listele(safeFirmaId, safeProjeId, safeLimit, safeOffset);
        List<ProjeKullaniciResponse> items = result.getItems().stream().map(this::toResponse).toList();
        return new PageResult<>(items, result.getTotal(), result.getLimit(), result.getOffset());
    }

    private Integer validatePositive(Integer value, String message) {
        if (value == null || value <= 0) {
            throw BusinessException.badRequest(message);
        }
        return value;
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) return DEFAULT_LIMIT;
        return Math.min(limit, MAX_LIMIT);
    }

    private ProjeKullaniciResponse toResponse(ProjeKullanici item) {
        return ProjeKullaniciResponse.builder()
                .projeId(item.getProjeId())
                .kullaniciId(item.getKullaniciId())
                .firmaId(item.getFirmaId())
                .ad(item.getAd())
                .soyad(item.getSoyad())
                .rol(item.getRol())
                .email(item.getEmail())
                .atanmaTarihi(item.getAtanmaTarihi())
                .build();
    }
}
