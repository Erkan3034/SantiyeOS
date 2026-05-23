package com.santiyeos.api.service.impl;

import com.santiyeos.api.dto.request.CreateOdemeRequest;
import com.santiyeos.api.dto.response.OdemeResponse;
import com.santiyeos.api.exception.BusinessException;
import com.santiyeos.api.model.Odeme;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.repository.OdemeRepository;
import com.santiyeos.api.service.OdemeService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class OdemeServiceImpl implements OdemeService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;
    private static final String DEFAULT_ODEME_YONTEMI = "HAVALE";
    private static final Set<String> ODEME_YONTEMLERI = Set.of("HAVALE", "EFT", "CEK", "NAKIT");
    private static final Set<String> ODEME_GORUNTULEME_ROLLERI = Set.of(
            "SUPER_ADMIN",
            "FIRMA_ADMIN",
            "PROJE_YONETICISI",
            "SAHA_PERSONELI",
            "TASERON_TEMSILCI"
    );

    private final OdemeRepository odemeRepository;

    public OdemeServiceImpl(OdemeRepository odemeRepository) {
        this.odemeRepository = odemeRepository;
    }

    @Override
    public PageResult<OdemeResponse> listele(
            Integer firmaId,
            Integer kullaniciId,
            String rol,
            Integer taseronId,
            Integer hakedisId,
            int limit,
            int offset
    ) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeKullaniciId = validateKullaniciId(kullaniciId);
        String safeRol = validateRol(rol);
        Integer safeTaseronId = validateOptionalPositive(taseronId, "Gecerli bir taseron id giriniz.");
        Integer safeHakedisId = validateOptionalPositive(hakedisId, "Gecerli bir hakedis id giriniz.");
        int safeLimit = normalizeLimit(limit);
        int safeOffset = Math.max(offset, 0);

        PageResult<Odeme> result = odemeRepository.listele(
                safeFirmaId,
                safeKullaniciId,
                safeRol,
                safeTaseronId,
                safeHakedisId,
                safeLimit,
                safeOffset
        );

        List<OdemeResponse> responseItems = result.getItems()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageResult<>(responseItems, result.getTotal(), result.getLimit(), result.getOffset());
    }

    @Override
    public OdemeResponse getir(Integer firmaId, Integer kullaniciId, String rol, Integer taseronId, Integer odemeId) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeKullaniciId = validateKullaniciId(kullaniciId);
        String safeRol = validateRol(rol);
        Integer safeTaseronId = validateOptionalPositive(taseronId, "Gecerli bir taseron id giriniz.");
        Integer safeOdemeId = validateOdemeId(odemeId);

        Odeme odeme = odemeRepository.getir(safeFirmaId, safeKullaniciId, safeRol, safeTaseronId, safeOdemeId);

        if (odeme == null) {
            throw BusinessException.notFound("Odeme bulunamadi.");
        }

        return toResponse(odeme);
    }

    @Override
    public OdemeResponse ekle(Integer firmaId, Integer kullaniciId, String rol, CreateOdemeRequest request) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeKullaniciId = validateKullaniciId(kullaniciId);
        String safeRol = validateRol(rol);

        Odeme odeme = Odeme.builder()
                .firmaId(safeFirmaId)
                .hakedisId(validatePositive(request.getHakedisId(), "Gecerli bir hakedis id giriniz."))
                .kaydedenId(safeKullaniciId)
                .tutar(validateTutar(request.getTutar()))
                .odemeTarihi(request.getOdemeTarihi())
                .odemeYontemi(normalizeOdemeYontemi(request.getOdemeYontemi()))
                .aciklama(request.getAciklama())
                .build();

        Integer odemeId = odemeRepository.ekle(safeFirmaId, safeKullaniciId, safeRol, odeme);

        if (odemeId == null) {
            throw BusinessException.conflict("Odeme olusturuldu ancak id alinamadi.");
        }

        return getir(safeFirmaId, safeKullaniciId, safeRol, null, odemeId);
    }

    @Override
    public void sil(Integer firmaId, Integer odemeId, Integer kullaniciId, String rol) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeOdemeId = validateOdemeId(odemeId);
        Integer safeKullaniciId = validateKullaniciId(kullaniciId);
        String safeRol = validateRol(rol);

        Integer etkilenenSatir = odemeRepository.sil(safeFirmaId, safeOdemeId, safeKullaniciId, safeRol);

        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("Odeme bulunamadi.");
        }
    }

    private Integer validateFirmaId(Integer firmaId) {
        return validatePositive(firmaId, "Gecerli bir firma id giriniz.");
    }

    private Integer validateOdemeId(Integer odemeId) {
        return validatePositive(odemeId, "Gecerli bir odeme id giriniz.");
    }

    private Integer validateKullaniciId(Integer kullaniciId) {
        return validatePositive(kullaniciId, "Gecerli bir kullanici id giriniz.");
    }

    private String validateRol(String rol) {
        if (rol == null || rol.isBlank()) {
            throw BusinessException.unauthorized("Gecerli kullanici rolu bulunamadi.");
        }

        String normalized = rol.trim().toUpperCase(Locale.ROOT);

        if (!ODEME_GORUNTULEME_ROLLERI.contains(normalized)) {
            throw BusinessException.forbidden("Bu islem icin yetkiniz yok.");
        }

        return normalized;
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

    private BigDecimal validateTutar(BigDecimal tutar) {
        if (tutar == null || tutar.compareTo(BigDecimal.ZERO) <= 0) {
            throw BusinessException.badRequest("Odeme tutari sifirdan buyuk olmalidir.");
        }

        return tutar;
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(limit, MAX_LIMIT);
    }

    private String normalizeOdemeYontemi(String odemeYontemi) {
        if (odemeYontemi == null || odemeYontemi.isBlank()) {
            return DEFAULT_ODEME_YONTEMI;
        }

        String normalized = odemeYontemi.trim().toUpperCase(Locale.ROOT);

        if (!ODEME_YONTEMLERI.contains(normalized)) {
            throw BusinessException.badRequest("Gecersiz odeme yontemi.");
        }

        return normalized;
    }

    private OdemeResponse toResponse(Odeme odeme) {
        return OdemeResponse.builder()
                .odemeId(odeme.getOdemeId())
                .firmaId(odeme.getFirmaId())
                .hakedisId(odeme.getHakedisId())
                .kaydedenId(odeme.getKaydedenId())
                .tutar(odeme.getTutar())
                .odemeTarihi(odeme.getOdemeTarihi())
                .odemeYontemi(odeme.getOdemeYontemi())
                .aciklama(odeme.getAciklama())
                .createdAt(odeme.getCreatedAt())
                .hakedisTutari(odeme.getHakedisTutari())
                .hakedisOnayDurumu(odeme.getHakedisOnayDurumu())
                .isEmriId(odeme.getIsEmriId())
                .isEmriBaslik(odeme.getIsEmriBaslik())
                .projeId(odeme.getProjeId())
                .projeAd(odeme.getProjeAd())
                .taseronId(odeme.getTaseronId())
                .taseronAd(odeme.getTaseronAd())
                .kaydeden(odeme.getKaydeden())
                .toplamOdenen(odeme.getToplamOdenen())
                .kalanTutar(odeme.getKalanTutar())
                .build();
    }
}
