package com.santiyeos.api.service.impl;

import com.santiyeos.api.dto.request.CreateHakedisRequest;
import com.santiyeos.api.dto.request.RejectHakedisRequest;
import com.santiyeos.api.dto.response.HakedisResponse;
import com.santiyeos.api.exception.BusinessException;
import com.santiyeos.api.model.Hakedis;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.repository.HakedisRepository;
import com.santiyeos.api.service.HakedisService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class HakedisServiceImpl implements HakedisService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;
    private static final Set<String> ONAY_DURUMLARI = Set.of("BEKLIYOR", "ONAYLANDI", "REDDEDILDI", "ITIRAZDA");

    private final HakedisRepository hakedisRepository;

    public HakedisServiceImpl(HakedisRepository hakedisRepository) {
        this.hakedisRepository = hakedisRepository;
    }

    @Override
    public PageResult<HakedisResponse> listele(Integer firmaId, Integer taseronId, String onayDurumu, int limit, int offset) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeTaseronId = validateOptionalPositive(taseronId, "Geçerli bir taşeron id giriniz.");
        String safeOnayDurumu = normalizeOnayDurumu(onayDurumu, true);
        int safeLimit = normalizeLimit(limit);
        int safeOffset = Math.max(offset, 0);

        PageResult<Hakedis> result = hakedisRepository.listele(
                safeFirmaId,
                safeTaseronId,
                safeOnayDurumu,
                safeLimit,
                safeOffset
        );

        List<HakedisResponse> responseItems = result.getItems()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageResult<>(responseItems, result.getTotal(), result.getLimit(), result.getOffset());
    }

    @Override
    public HakedisResponse getir(Integer firmaId,Integer taseronId, Integer hakedisId) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeTaseronId = validateOptionalPositive(taseronId, "Geçerli bir taseron ID giriniz.");
        Integer safeHakedisId = validateHakedisId(hakedisId);

        Hakedis hakedis = hakedisRepository.getir(safeFirmaId,safeTaseronId, safeHakedisId);

        if (hakedis == null) {
            throw BusinessException.notFound("Hakediş bulunamadı.");
        }

        return toResponse(hakedis);
    }

    @Override
    public HakedisResponse ekle(Integer firmaId, Integer kullaniciId, CreateHakedisRequest request) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeKullaniciId = validateKullaniciId(kullaniciId);

        Hakedis hakedis = Hakedis.builder()
                .firmaId(safeFirmaId)
                .isEmriId(validatePositive(request.getIsEmriId(), "Geçerli bir iş emri id giriniz."))
                .talepEdenId(safeKullaniciId)
                .tutar(validateTutar(request.getTutar()))
                .aciklama(request.getAciklama())
                .build();

        Integer hakedisId = hakedisRepository.ekle(safeFirmaId, safeKullaniciId, hakedis);

        if (hakedisId == null) {
            throw BusinessException.conflict("Hakediş oluşturuldu ancak id alınamadı.");
        }

        return getir(safeFirmaId,null, hakedisId);
    }

    @Override
    public HakedisResponse onayla(Integer firmaId, Integer hakedisId, Integer kullaniciId) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeHakedisId = validateHakedisId(hakedisId);
        Integer safeKullaniciId = validateKullaniciId(kullaniciId);

        Integer etkilenenSatir = hakedisRepository.onayla(safeFirmaId, safeHakedisId, safeKullaniciId);

        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("Hakediş bulunamadı.");
        }

        return getir(safeFirmaId,null, safeHakedisId);
    }

    @Override
    public HakedisResponse reddet(Integer firmaId, Integer hakedisId, Integer kullaniciId, RejectHakedisRequest request) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeHakedisId = validateHakedisId(hakedisId);
        Integer safeKullaniciId = validateKullaniciId(kullaniciId);

        Integer etkilenenSatir = hakedisRepository.reddet(
                safeFirmaId,
                safeHakedisId,
                safeKullaniciId,
                request.getRedGerekce()
        );

        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("Hakediş bulunamadı.");
        }

        return getir(safeFirmaId,null, safeHakedisId);
    }

    @Override
    public void sil(Integer firmaId, Integer hakedisId, Integer kullaniciId) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeHakedisId = validateHakedisId(hakedisId);
        Integer safeKullaniciId = validateKullaniciId(kullaniciId);

        Integer etkilenenSatir = hakedisRepository.sil(safeFirmaId, safeHakedisId, safeKullaniciId);

        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("Hakediş bulunamadı.");
        }
    }

    private Integer validateFirmaId(Integer firmaId) {
        return validatePositive(firmaId, "Geçerli bir firma id giriniz.");
    }

    private Integer validateHakedisId(Integer hakedisId) {
        return validatePositive(hakedisId, "Geçerli bir hakediş id giriniz.");
    }

    private Integer validateKullaniciId(Integer kullaniciId) {
        return validatePositive(kullaniciId, "Geçerli bir kullanıcı id giriniz.");
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
            throw BusinessException.badRequest("Hakediş tutarı sıfırdan büyük olmalıdır.");
        }

        return tutar;
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(limit, MAX_LIMIT);
    }

    private String normalizeOnayDurumu(String onayDurumu, boolean nullableAllowed) {
        if (onayDurumu == null || onayDurumu.isBlank()) {
            if (nullableAllowed) {
                return null;
            }

            throw BusinessException.badRequest("Hakediş onay durumu zorunludur.");
        }

        String normalized = onayDurumu.trim().toUpperCase(Locale.ROOT);

        if (!ONAY_DURUMLARI.contains(normalized)) {
            throw BusinessException.badRequest("Geçersiz hakediş onay durumu.");
        }

        return normalized;
    }

    private HakedisResponse toResponse(Hakedis hakedis) {
        return HakedisResponse.builder()
                .hakedisId(hakedis.getHakedisId())
                .firmaId(hakedis.getFirmaId())
                .isEmriId(hakedis.getIsEmriId())
                .talepEdenId(hakedis.getTalepEdenId())
                .onaylayanId(hakedis.getOnaylayanId())
                .tutar(hakedis.getTutar())
                .onayDurumu(hakedis.getOnayDurumu())
                .onayTarihi(hakedis.getOnayTarihi())
                .aciklama(hakedis.getAciklama())
                .redGerekce(hakedis.getRedGerekce())
                .createdAt(hakedis.getCreatedAt())
                .updatedAt(hakedis.getUpdatedAt())
                .projeId(hakedis.getProjeId())
                .projeAd(hakedis.getProjeAd())
                .taseronId(hakedis.getTaseronId())
                .taseronAd(hakedis.getTaseronAd())
                .isEmriBaslik(hakedis.getIsEmriBaslik())
                .tamamlanmaTarihi(hakedis.getTamamlanmaTarihi())
                .talepEden(hakedis.getTalepEden())
                .onaylayan(hakedis.getOnaylayan())
                .odenenTutar(hakedis.getOdenenTutar())
                .kalanTutar(hakedis.getKalanTutar())
                .build();
    }
}
