package com.santiyeos.api.service.impl;

import com.santiyeos.api.dto.request.CreateStokHareketRequest;
import com.santiyeos.api.dto.response.StokHareketResponse;
import com.santiyeos.api.exception.BusinessException;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.model.StokHareket;
import com.santiyeos.api.repository.StokHareketRepository;
import com.santiyeos.api.service.StokHareketService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class StokHareketServiceImpl implements StokHareketService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;
    private static final Set<String> HAREKET_TIPLERI = Set.of("GIRIS", "CIKIS");
    private static final Set<String> YAZMA_ROLLERI = Set.of(
            "SUPER_ADMIN",
            "FIRMA_ADMIN",
            "PROJE_YONETICISI",
            "SAHA_PERSONELI"
    );

    private final StokHareketRepository stokHareketRepository;

    public StokHareketServiceImpl(StokHareketRepository stokHareketRepository) {
        this.stokHareketRepository = stokHareketRepository;
    }

    @Override
    public PageResult<StokHareketResponse> listele(
            Integer firmaId,
            Integer malzemeId,
            Integer projeId,
            Integer isEmriId,
            String hareketTipi,
            int limit,
            int offset
    ) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeMalzemeId = validateOptionalPositive(malzemeId, "Gecerli bir malzeme id giriniz.");
        Integer safeProjeId = validateOptionalPositive(projeId, "Gecerli bir proje id giriniz.");
        Integer safeIsEmriId = validateOptionalPositive(isEmriId, "Gecerli bir is emri id giriniz.");
        String safeHareketTipi = normalizeOptionalHareketTipi(hareketTipi);
        int safeLimit = normalizeLimit(limit);
        int safeOffset = Math.max(offset, 0);

        PageResult<StokHareket> result = stokHareketRepository.listele(
                safeFirmaId,
                safeMalzemeId,
                safeProjeId,
                safeIsEmriId,
                safeHareketTipi,
                safeLimit,
                safeOffset
        );
        List<StokHareketResponse> items = result.getItems().stream().map(this::toResponse).toList();

        return new PageResult<>(items, result.getTotal(), result.getLimit(), result.getOffset());
    }

    @Override
    public StokHareketResponse getir(Integer firmaId, Integer hareketId) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeHareketId = validatePositive(hareketId, "Gecerli bir stok hareket id giriniz.");

        StokHareket hareket = stokHareketRepository.getir(safeFirmaId, safeHareketId);

        if (hareket == null) {
            throw BusinessException.notFound("Stok hareketi bulunamadi.");
        }

        return toResponse(hareket);
    }

    @Override
    public StokHareketResponse ekle(
            Integer firmaId,
            Integer kaydedenId,
            String rol,
            CreateStokHareketRequest request
    ) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeKaydedenId = validatePositive(kaydedenId, "Gecerli bir kullanici id giriniz.");
        String safeRol = validateWriteRol(rol);

        StokHareket hareket = StokHareket.builder()
                .firmaId(safeFirmaId)
                .malzemeId(validatePositive(request.getMalzemeId(), "Gecerli bir malzeme id giriniz."))
                .projeId(validateOptionalPositive(request.getProjeId(), "Gecerli bir proje id giriniz."))
                .isEmriId(validateOptionalPositive(request.getIsEmriId(), "Gecerli bir is emri id giriniz."))
                .kaydedenId(safeKaydedenId)
                .hareketTipi(normalizeRequiredHareketTipi(request.getHareketTipi()))
                .miktar(validateMiktar(request.getMiktar()))
                .birimFiyat(validateNullableDecimal(request.getBirimFiyat()))
                .aciklama(request.getAciklama())
                .build();

        Integer hareketId = stokHareketRepository.ekle(safeFirmaId, safeKaydedenId, safeRol, hareket);

        if (hareketId == null) {
            throw BusinessException.conflict("Stok hareketi olusturuldu ancak id alinamadi.");
        }

        return getir(safeFirmaId, hareketId);
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

    private Integer validateOptionalPositive(Integer value, String message) {
        if (value == null) {
            return null;
        }

        return validatePositive(value, message);
    }

    private String validateWriteRol(String rol) {
        if (rol == null || rol.isBlank()) {
            throw BusinessException.unauthorized("Gecerli kullanici rolu bulunamadi.");
        }

        String normalized = rol.trim().toUpperCase(Locale.ROOT);

        if (!YAZMA_ROLLERI.contains(normalized)) {
            throw BusinessException.forbidden("Bu islem icin yetkiniz yok.");
        }

        return normalized;
    }

    private String normalizeRequiredHareketTipi(String hareketTipi) {
        String normalized = normalizeOptionalHareketTipi(hareketTipi);

        if (normalized == null) {
            throw BusinessException.badRequest("Stok hareket tipi zorunludur.");
        }

        return normalized;
    }

    private String normalizeOptionalHareketTipi(String hareketTipi) {
        if (hareketTipi == null || hareketTipi.isBlank()) {
            return null;
        }

        String normalized = hareketTipi.trim().toUpperCase(Locale.ROOT);

        if (!HAREKET_TIPLERI.contains(normalized)) {
            throw BusinessException.badRequest("Gecersiz stok hareket tipi.");
        }

        return normalized;
    }

    private BigDecimal validateMiktar(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw BusinessException.badRequest("Miktar sifirdan buyuk olmalidir.");
        }

        return value;
    }

    private BigDecimal validateNullableDecimal(BigDecimal value) {
        if (value == null) {
            return null;
        }

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.badRequest("Birim fiyat negatif olamaz.");
        }

        return value;
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(limit, MAX_LIMIT);
    }

    private StokHareketResponse toResponse(StokHareket hareket) {
        return StokHareketResponse.builder()
                .hareketId(hareket.getHareketId())
                .firmaId(hareket.getFirmaId())
                .malzemeId(hareket.getMalzemeId())
                .projeId(hareket.getProjeId())
                .isEmriId(hareket.getIsEmriId())
                .kaydedenId(hareket.getKaydedenId())
                .hareketTipi(hareket.getHareketTipi())
                .miktar(hareket.getMiktar())
                .birimFiyat(hareket.getBirimFiyat())
                .aciklama(hareket.getAciklama())
                .createdAt(hareket.getCreatedAt())
                .malzemeAd(hareket.getMalzemeAd())
                .birim(hareket.getBirim())
                .projeAd(hareket.getProjeAd())
                .isEmriBaslik(hareket.getIsEmriBaslik())
                .kaydeden(hareket.getKaydeden())
                .build();
    }
}
