package com.santiyeos.api.service.impl;

import com.santiyeos.api.dto.request.CreateMalzemeRequest;
import com.santiyeos.api.dto.request.UpdateMalzemeRequest;
import com.santiyeos.api.dto.response.MalzemeResponse;
import com.santiyeos.api.exception.BusinessException;
import com.santiyeos.api.model.Malzeme;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.repository.MalzemeRepository;
import com.santiyeos.api.service.MalzemeService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class MalzemeServiceImpl implements MalzemeService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;
    private static final String DEFAULT_BIRIM = "ADET";
    private static final Set<String> BIRIMLER = Set.of("ADET", "KG", "TON", "METRE", "M2", "M3", "LITRE");
    private static final Set<String> YAZMA_ROLLERI = Set.of("SUPER_ADMIN", "FIRMA_ADMIN", "PROJE_YONETICISI");

    private final MalzemeRepository malzemeRepository;

    public MalzemeServiceImpl(MalzemeRepository malzemeRepository) {
        this.malzemeRepository = malzemeRepository;
    }

    @Override
    public PageResult<MalzemeResponse> listele(
            Integer firmaId,
            Integer kategoriId,
            Boolean kritikStok,
            int limit,
            int offset
    ) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeKategoriId = validateOptionalPositive(kategoriId, "Gecerli bir kategori id giriniz.");
        int safeLimit = normalizeLimit(limit);
        int safeOffset = Math.max(offset, 0);

        PageResult<Malzeme> result = malzemeRepository.listele(
                safeFirmaId,
                safeKategoriId,
                kritikStok,
                safeLimit,
                safeOffset
        );
        List<MalzemeResponse> items = result.getItems().stream().map(this::toResponse).toList();

        return new PageResult<>(items, result.getTotal(), result.getLimit(), result.getOffset());
    }

    @Override
    public MalzemeResponse getir(Integer firmaId, Integer malzemeId) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeMalzemeId = validatePositive(malzemeId, "Gecerli bir malzeme id giriniz.");

        Malzeme malzeme = malzemeRepository.getir(safeFirmaId, safeMalzemeId);

        if (malzeme == null) {
            throw BusinessException.notFound("Malzeme bulunamadi.");
        }

        return toResponse(malzeme);
    }

    @Override
    public MalzemeResponse ekle(Integer firmaId, String rol, CreateMalzemeRequest request) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        String safeRol = validateWriteRol(rol);

        Malzeme malzeme = Malzeme.builder()
                .firmaId(safeFirmaId)
                .kategoriId(validateOptionalPositive(request.getKategoriId(), "Gecerli bir kategori id giriniz."))
                .ad(request.getAd())
                .birim(normalizeBirim(request.getBirim()))
                .birimFiyat(normalizeDecimal(request.getBirimFiyat()))
                .minStok(normalizeDecimal(request.getMinStok()))
                .build();

        Integer malzemeId = malzemeRepository.ekle(safeFirmaId, safeRol, malzeme);

        if (malzemeId == null) {
            throw BusinessException.conflict("Malzeme olusturuldu ancak id alinamadi.");
        }

        return getir(safeFirmaId, malzemeId);
    }

    @Override
    public MalzemeResponse guncelle(Integer firmaId, Integer malzemeId, String rol, UpdateMalzemeRequest request) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeMalzemeId = validatePositive(malzemeId, "Gecerli bir malzeme id giriniz.");
        String safeRol = validateWriteRol(rol);

        Malzeme malzeme = Malzeme.builder()
                .kategoriId(validateOptionalPositive(request.getKategoriId(), "Gecerli bir kategori id giriniz."))
                .ad(request.getAd())
                .birim(normalizeBirim(request.getBirim()))
                .birimFiyat(normalizeDecimal(request.getBirimFiyat()))
                .minStok(normalizeDecimal(request.getMinStok()))
                .build();

        Integer etkilenenSatir = malzemeRepository.guncelle(safeFirmaId, safeMalzemeId, safeRol, malzeme);

        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("Malzeme bulunamadi.");
        }

        return getir(safeFirmaId, safeMalzemeId);
    }

    @Override
    public void sil(Integer firmaId, Integer malzemeId, Integer kullaniciId, String rol) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeMalzemeId = validatePositive(malzemeId, "Gecerli bir malzeme id giriniz.");
        Integer safeKullaniciId = validatePositive(kullaniciId, "Gecerli bir kullanici id giriniz.");
        String safeRol = validateWriteRol(rol);

        Integer etkilenenSatir = malzemeRepository.sil(safeFirmaId, safeMalzemeId, safeKullaniciId, safeRol);

        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("Malzeme bulunamadi.");
        }
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

    private String normalizeBirim(String birim) {
        if (birim == null || birim.isBlank()) {
            return DEFAULT_BIRIM;
        }

        String normalized = birim.trim().toUpperCase(Locale.ROOT);

        if (!BIRIMLER.contains(normalized)) {
            throw BusinessException.badRequest("Gecersiz malzeme birimi.");
        }

        return normalized;
    }

    private BigDecimal normalizeDecimal(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.badRequest("Sayisal deger negatif olamaz.");
        }

        return value;
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(limit, MAX_LIMIT);
    }

    private MalzemeResponse toResponse(Malzeme malzeme) {
        return MalzemeResponse.builder()
                .malzemeId(malzeme.getMalzemeId())
                .firmaId(malzeme.getFirmaId())
                .kategoriId(malzeme.getKategoriId())
                .kategoriAd(malzeme.getKategoriAd())
                .ad(malzeme.getAd())
                .birim(malzeme.getBirim())
                .birimFiyat(malzeme.getBirimFiyat())
                .stokMiktari(malzeme.getStokMiktari())
                .minStok(malzeme.getMinStok())
                .kritikStok(malzeme.getKritikStok())
                .aktif(malzeme.getAktif())
                .createdAt(malzeme.getCreatedAt())
                .updatedAt(malzeme.getUpdatedAt())
                .build();
    }
}
