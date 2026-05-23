package com.santiyeos.api.service.impl;

import com.santiyeos.api.dto.request.CreateBildirimRequest;
import com.santiyeos.api.dto.response.BildirimResponse;
import com.santiyeos.api.exception.BusinessException;
import com.santiyeos.api.model.Bildirim;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.repository.BildirimRepository;
import com.santiyeos.api.service.BildirimService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class BildirimServiceImpl implements BildirimService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;
    private static final Set<String> BILDIRIM_TIPLERI = Set.of(
            "IS_EMRI",
            "HAKEDIS",
            "ODEME",
            "BUTCE",
            "SISTEM"
    );

    private final BildirimRepository bildirimRepository;

    public BildirimServiceImpl(BildirimRepository bildirimRepository) {
        this.bildirimRepository = bildirimRepository;
    }

    @Override
    public PageResult<BildirimResponse> listele(
            Integer firmaId,
            Integer kullaniciId,
            Boolean sadeceOkunmamis,
            int limit,
            int offset
    ) {
        Integer safeFirmaId = validatePositive(firmaId, "Gecerli bir firma id giriniz.");
        Integer safeKullaniciId = validatePositive(kullaniciId, "Gecerli bir kullanici id giriniz.");
        int safeLimit = normalizeLimit(limit);
        int safeOffset = Math.max(offset, 0);

        PageResult<Bildirim> result = bildirimRepository.listele(
                safeFirmaId,
                safeKullaniciId,
                Boolean.TRUE.equals(sadeceOkunmamis),
                safeLimit,
                safeOffset
        );
        List<BildirimResponse> items = result.getItems().stream().map(this::toResponse).toList();

        return new PageResult<>(items, result.getTotal(), result.getLimit(), result.getOffset());
    }

    @Override
    public BildirimResponse getir(Integer firmaId, Integer kullaniciId, Integer bildirimId) {
        Integer safeFirmaId = validatePositive(firmaId, "Gecerli bir firma id giriniz.");
        Integer safeKullaniciId = validatePositive(kullaniciId, "Gecerli bir kullanici id giriniz.");
        Integer safeBildirimId = validatePositive(bildirimId, "Gecerli bir bildirim id giriniz.");

        Bildirim bildirim = bildirimRepository.getir(safeFirmaId, safeKullaniciId, safeBildirimId);

        if (bildirim == null) {
            throw BusinessException.notFound("Bildirim bulunamadi.");
        }

        return toResponse(bildirim);
    }

    @Override
    public BildirimResponse ekle(Integer firmaId, Integer olusturanId, CreateBildirimRequest request) {
        Integer safeFirmaId = validatePositive(firmaId, "Gecerli bir firma id giriniz.");
        Integer safeOlusturanId = validatePositive(olusturanId, "Gecerli bir kullanici id giriniz.");
        Integer hedefKullaniciId = validatePositive(request.getKullaniciId(), "Gecerli bir hedef kullanici id giriniz.");

        Bildirim bildirim = Bildirim.builder()
                .baslik(request.getBaslik())
                .mesaj(request.getMesaj())
                .tip(normalizeTip(request.getTip()))
                .referansTablo(normalizeBlankToNull(request.getReferansTablo()))
                .referansId(request.getReferansId())
                .build();

        Integer bildirimId = bildirimRepository.ekle(safeFirmaId, hedefKullaniciId, safeOlusturanId, bildirim);

        if (bildirimId == null) {
            throw BusinessException.conflict("Bildirim olusturuldu ancak id alinamadi.");
        }

        return getir(safeFirmaId, hedefKullaniciId, bildirimId);
    }

    @Override
    public BildirimResponse okunduIsaretle(Integer firmaId, Integer kullaniciId, Integer bildirimId) {
        Integer safeFirmaId = validatePositive(firmaId, "Gecerli bir firma id giriniz.");
        Integer safeKullaniciId = validatePositive(kullaniciId, "Gecerli bir kullanici id giriniz.");
        Integer safeBildirimId = validatePositive(bildirimId, "Gecerli bir bildirim id giriniz.");

        Integer etkilenenSatir = bildirimRepository.okunduIsaretle(safeFirmaId, safeKullaniciId, safeBildirimId);

        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("Bildirim bulunamadi.");
        }

        return getir(safeFirmaId, safeKullaniciId, safeBildirimId);
    }

    @Override
    public Integer tumunuOkunduIsaretle(Integer firmaId, Integer kullaniciId) {
        Integer safeFirmaId = validatePositive(firmaId, "Gecerli bir firma id giriniz.");
        Integer safeKullaniciId = validatePositive(kullaniciId, "Gecerli bir kullanici id giriniz.");

        return bildirimRepository.tumunuOkunduIsaretle(safeFirmaId, safeKullaniciId);
    }

    @Override
    public void sil(Integer firmaId, Integer kullaniciId, Integer bildirimId) {
        Integer safeFirmaId = validatePositive(firmaId, "Gecerli bir firma id giriniz.");
        Integer safeKullaniciId = validatePositive(kullaniciId, "Gecerli bir kullanici id giriniz.");
        Integer safeBildirimId = validatePositive(bildirimId, "Gecerli bir bildirim id giriniz.");

        Integer etkilenenSatir = bildirimRepository.sil(safeFirmaId, safeKullaniciId, safeBildirimId);

        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("Bildirim bulunamadi.");
        }
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

    private String normalizeTip(String tip) {
        String normalized = tip == null ? null : tip.trim().toUpperCase(Locale.ROOT);

        if (normalized == null || !BILDIRIM_TIPLERI.contains(normalized)) {
            throw BusinessException.badRequest("Gecersiz bildirim tipi.");
        }

        return normalized;
    }

    private String normalizeBlankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private BildirimResponse toResponse(Bildirim bildirim) {
        return BildirimResponse.builder()
                .bildirimId(bildirim.getBildirimId())
                .firmaId(bildirim.getFirmaId())
                .kullaniciId(bildirim.getKullaniciId())
                .baslik(bildirim.getBaslik())
                .mesaj(bildirim.getMesaj())
                .tip(bildirim.getTip())
                .referansTablo(bildirim.getReferansTablo())
                .referansId(bildirim.getReferansId())
                .okundu(bildirim.getOkundu())
                .okunduTarihi(bildirim.getOkunduTarihi())
                .createdAt(bildirim.getCreatedAt())
                .build();
    }
}
