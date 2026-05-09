package com.santiyeos.api.service.impl;

import com.santiyeos.api.dto.request.CreateIsEmriRequest;
import com.santiyeos.api.dto.request.UpdateIsEmriDurumRequest;
import com.santiyeos.api.dto.request.UpdateIsEmriRequest;
import com.santiyeos.api.dto.response.IsEmriResponse;
import com.santiyeos.api.exception.BusinessException;
import com.santiyeos.api.model.IsEmri;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.repository.IsEmriRepository;
import com.santiyeos.api.service.IsEmriService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class IsEmriServiceImpl implements IsEmriService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;
    private static final String DEFAULT_ONCELIK = "NORMAL";

    private static final Set<String> ONCELIKLER = Set.of("DUSUK", "NORMAL", "YUKSEK", "KRITIK");
    private static final Set<String> LISTE_DURUMLARI = Set.of("BEKLIYOR", "BASLADI", "DEVAM_EDIYOR", "TAMAMLANDI", "IPTAL", "HAKEDISTE");
    private static final Set<String> GUNCELLEME_DURUMLARI = Set.of("BEKLIYOR", "BASLADI", "DEVAM_EDIYOR", "TAMAMLANDI", "IPTAL");

    private final IsEmriRepository isEmriRepository;

    public IsEmriServiceImpl(IsEmriRepository isEmriRepository) {
        this.isEmriRepository = isEmriRepository;
    }

    @Override
    public PageResult<IsEmriResponse> listele(Integer firmaId, Integer projeId, String durum, int limit, int offset) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeProjeId = validateOptionalPositive(projeId, "Geçerli bir proje id giriniz.");
        String safeDurum = normalizeDurum(durum, LISTE_DURUMLARI, true);
        int safeLimit = normalizeLimit(limit);
        int safeOffset = Math.max(offset, 0);

        PageResult<IsEmri> result = isEmriRepository.listele(safeFirmaId, safeProjeId, safeDurum, safeLimit, safeOffset);

        List<IsEmriResponse> responseItems = result.getItems()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageResult<>(responseItems, result.getTotal(), result.getLimit(), result.getOffset());
    }

    @Override
    public IsEmriResponse getir(Integer firmaId, Integer isEmriId) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeIsEmriId = validateIsEmriId(isEmriId);

        IsEmri isEmri = isEmriRepository.getir(safeFirmaId, safeIsEmriId);

        if (isEmri == null) {
            throw BusinessException.notFound("İş emri bulunamadı.");
        }

        return toResponse(isEmri);
    }

    @Override
    public IsEmriResponse ekle(Integer firmaId, Integer kullaniciId, CreateIsEmriRequest request) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeKullaniciId = validateKullaniciId(kullaniciId);
        validateDateRange(request.getBaslangicTarihi(), request.getBitisTarihi());

        IsEmri isEmri = IsEmri.builder()
                .firmaId(safeFirmaId)
                .projeId(validatePositive(request.getProjeId(), "Geçerli bir proje id giriniz."))
                .taseronId(validatePositive(request.getTaseronId(), "Geçerli bir taşeron id giriniz."))
                .atananKullaniciId(validateOptionalPositive(request.getAtananKullaniciId(), "Geçerli bir atanan kullanıcı id giriniz."))
                .olusturanId(safeKullaniciId)
                .baslik(request.getBaslik())
                .aciklama(request.getAciklama())
                .oncelik(normalizeOncelik(request.getOncelik(), true))
                .baslangicTarihi(request.getBaslangicTarihi())
                .bitisTarihi(request.getBitisTarihi())
                .build();

        Integer isEmriId = isEmriRepository.ekle(safeFirmaId, safeKullaniciId, isEmri);

        if (isEmriId == null) {
            throw BusinessException.conflict("İş emri oluşturuldu ancak id alınamadı.");
        }

        return getir(safeFirmaId, isEmriId);
    }

    @Override
    public IsEmriResponse guncelle(Integer firmaId, Integer isEmriId, Integer kullaniciId, UpdateIsEmriRequest request) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeIsEmriId = validateIsEmriId(isEmriId);
        Integer safeKullaniciId = validateKullaniciId(kullaniciId);
        validateDateRange(request.getBaslangicTarihi(), request.getBitisTarihi());

        IsEmri isEmri = IsEmri.builder()
                .atananKullaniciId(validateOptionalPositive(request.getAtananKullaniciId(), "Geçerli bir atanan kullanıcı id giriniz."))
                .baslik(request.getBaslik())
                .aciklama(request.getAciklama())
                .oncelik(normalizeOncelik(request.getOncelik(), false))
                .baslangicTarihi(request.getBaslangicTarihi())
                .bitisTarihi(request.getBitisTarihi())
                .build();

        Integer etkilenenSatir = isEmriRepository.guncelle(safeFirmaId, safeIsEmriId, safeKullaniciId, isEmri);

        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("İş emri bulunamadı.");
        }

        return getir(safeFirmaId, safeIsEmriId);
    }

    @Override
    public IsEmriResponse durumGuncelle(Integer firmaId, Integer isEmriId, Integer kullaniciId, UpdateIsEmriDurumRequest request) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeIsEmriId = validateIsEmriId(isEmriId);
        Integer safeKullaniciId = validateKullaniciId(kullaniciId);
        String safeDurum = normalizeDurum(request.getDurum(), GUNCELLEME_DURUMLARI, false);

        Integer etkilenenSatir = isEmriRepository.durumGuncelle(
                safeFirmaId,
                safeIsEmriId,
                safeKullaniciId,
                safeDurum,
                request.getAciklama()
        );

        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("İş emri bulunamadı.");
        }

        return getir(safeFirmaId, safeIsEmriId);
    }

    @Override
    public void sil(Integer firmaId, Integer isEmriId, Integer kullaniciId) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeIsEmriId = validateIsEmriId(isEmriId);
        Integer safeKullaniciId = validateKullaniciId(kullaniciId);

        Integer etkilenenSatir = isEmriRepository.sil(safeFirmaId, safeIsEmriId, safeKullaniciId);

        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("İş emri bulunamadı.");
        }
    }

    private Integer validateFirmaId(Integer firmaId) {
        return validatePositive(firmaId, "Geçerli bir firma id giriniz.");
    }

    private Integer validateIsEmriId(Integer isEmriId) {
        return validatePositive(isEmriId, "Geçerli bir iş emri id giriniz.");
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

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(limit, MAX_LIMIT);
    }

    private String normalizeOncelik(String oncelik, boolean defaultAllowed) {
        if (oncelik == null || oncelik.isBlank()) {
            if (defaultAllowed) {
                return DEFAULT_ONCELIK;
            }

            throw BusinessException.badRequest("İş emri önceliği zorunludur.");
        }

        String normalized = oncelik.trim().toUpperCase(Locale.ROOT);

        if (!ONCELIKLER.contains(normalized)) {
            throw BusinessException.badRequest("Geçersiz iş emri önceliği.");
        }

        return normalized;
    }

    private String normalizeDurum(String durum, Set<String> allowedStatuses, boolean nullableAllowed) {
        if (durum == null || durum.isBlank()) {
            if (nullableAllowed) {
                return null;
            }

            throw BusinessException.badRequest("İş emri durumu zorunludur.");
        }

        String normalized = durum.trim().toUpperCase(Locale.ROOT);

        if (!allowedStatuses.contains(normalized)) {
            throw BusinessException.badRequest("Geçersiz iş emri durumu.");
        }

        return normalized;
    }

    private void validateDateRange(LocalDate baslangic, LocalDate bitis) {
        if (baslangic != null && bitis != null && bitis.isBefore(baslangic)) {
            throw BusinessException.badRequest("Bitiş tarihi başlangıç tarihinden önce olamaz.");
        }
    }

    private IsEmriResponse toResponse(IsEmri isEmri) {
        return IsEmriResponse.builder()
                .isEmriId(isEmri.getIsEmriId())
                .firmaId(isEmri.getFirmaId())
                .projeId(isEmri.getProjeId())
                .taseronId(isEmri.getTaseronId())
                .atananKullaniciId(isEmri.getAtananKullaniciId())
                .olusturanId(isEmri.getOlusturanId())
                .baslik(isEmri.getBaslik())
                .aciklama(isEmri.getAciklama())
                .oncelik(isEmri.getOncelik())
                .durum(isEmri.getDurum())
                .baslangicTarihi(isEmri.getBaslangicTarihi())
                .bitisTarihi(isEmri.getBitisTarihi())
                .tamamlanmaTarihi(isEmri.getTamamlanmaTarihi())
                .createdAt(isEmri.getCreatedAt())
                .updatedAt(isEmri.getUpdatedAt())
                .projeAd(isEmri.getProjeAd())
                .taseronAd(isEmri.getTaseronAd())
                .taseronUzmanlik(isEmri.getTaseronUzmanlik())
                .atananKullanici(isEmri.getAtananKullanici())
                .olusturan(isEmri.getOlusturan())
                .notSayisi(isEmri.getNotSayisi())
                .raporSayisi(isEmri.getRaporSayisi())
                .kalanGun(isEmri.getKalanGun())
                .build();
    }
}
