package com.santiyeos.api.service.impl;

import com.santiyeos.api.dto.request.CreateProjeRequest;
import com.santiyeos.api.dto.request.UpdateProjeRequest;
import com.santiyeos.api.dto.response.ProjeResponse;
import com.santiyeos.api.exception.BusinessException;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.model.Proje;
import com.santiyeos.api.repository.ProjeRepository;
import com.santiyeos.api.service.ProjeService;
import org.springframework.stereotype.Service;
import com.santiyeos.api.service.AbonelikLimitService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ProjeServiceImpl implements ProjeService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;
    private static final int DEFAULT_BUTCE_UYARI_YUZDE = 85;
    private final AbonelikLimitService abonelikLimitService;
    private static final Set<String> PROJE_DURUMLARI = Set.of(
            "PLANLANDI",
            "DEVAM_EDIYOR",
            "TAMAMLANDI",
            "IPTAL"
    );
    private static final Set<String> PROJE_OKUMA_ROLLERI = Set.of(
            "SUPER_ADMIN",
            "FIRMA_ADMIN",
            "PROJE_YONETICISI"
    );

    private final ProjeRepository projeRepository;

    public ProjeServiceImpl(AbonelikLimitService abonelikLimitService, ProjeRepository projeRepository) {
        this.abonelikLimitService = abonelikLimitService;
        this.projeRepository = projeRepository;
    }

    @Override
    public PageResult<ProjeResponse> listele(Integer firmaId, Integer kullaniciId, String rol, String durum, int limit, int offset) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        String safeRol = validateProjeOkumaRolu(rol);
        Integer safeKullaniciId = validateKullaniciIdIfRequired(kullaniciId, safeRol);
        String safeDurum = normalizeDurum(durum);
        int safeLimit = normalizeLimit(limit);
        int safeOffset = Math.max(offset, 0);

        PageResult<Proje> result = projeRepository.listele(
                safeFirmaId,
                safeKullaniciId,
                safeRol,
                safeDurum,
                safeLimit,
                safeOffset
        );

        List<ProjeResponse> responseItems = result.getItems()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageResult<>(
                responseItems,
                result.getTotal(),
                result.getLimit(),
                result.getOffset()
        );
    }

    @Override
    public ProjeResponse getir(Integer firmaId, Integer kullaniciId, String rol, Integer projeId) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        String safeRol = validateProjeOkumaRolu(rol);
        Integer safeKullaniciId = validateKullaniciIdIfRequired(kullaniciId, safeRol);
        Integer safeProjeId = validateProjeId(projeId);

        Proje proje = projeRepository.getir(safeFirmaId, safeKullaniciId, safeRol, safeProjeId);

        if (proje == null) {
            throw BusinessException.notFound("Proje bulunamadı.");
        }

        return toResponse(proje);
    }

    @Override
    public ProjeResponse ekle(Integer firmaId, CreateProjeRequest request) {
        Integer safeFirmaId = validateFirmaId(firmaId);

        Proje proje = Proje.builder()
                .firmaId(safeFirmaId)
                .ad(request.getAd())
                .aciklama(request.getAciklama())
                .konum(request.getKonum())
                .butce(normalizeButce(request.getButce()))
                .baslangicTarihi(request.getBaslangicTarihi())
                .bitisTarihi(request.getBitisTarihi())
                .butceUyariYuzde(normalizeButceUyariYuzde(request.getButceUyariYuzde()))
                .build();
        abonelikLimitService.projeEklemeHakkiKontrolEt(safeFirmaId);
        Integer projeId = projeRepository.ekle(safeFirmaId, proje);

        if (projeId == null) {
            throw BusinessException.conflict("Proje oluşturuldu ancak id alınamadı.");
        }

        return getir(safeFirmaId, null, "FIRMA_ADMIN", projeId);
    }

    @Override
    public ProjeResponse guncelle(Integer firmaId, Integer projeId, UpdateProjeRequest request) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeProjeId = validateProjeId(projeId);

        Proje proje = Proje.builder()
                .firmaId(safeFirmaId)
                .projeId(safeProjeId)
                .ad(request.getAd())
                .aciklama(request.getAciklama())
                .konum(request.getKonum())
                .butce(normalizeButce(request.getButce()))
                .baslangicTarihi(request.getBaslangicTarihi())
                .bitisTarihi(request.getBitisTarihi())
                .durum(validateRequiredDurum(request.getDurum()))
                .butceUyariYuzde(normalizeButceUyariYuzde(request.getButceUyariYuzde()))
                .build();

        Integer etkilenenSatir = projeRepository.guncelle(safeFirmaId, safeProjeId, proje);

        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("Proje bulunamadı.");
        }

        return getir(safeFirmaId, null, "FIRMA_ADMIN", safeProjeId);
    }

    @Override
    public void sil(Integer firmaId, Integer projeId, Integer kullaniciId) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeProjeId = validateProjeId(projeId);
        Integer safeKullaniciId = validateKullaniciId(kullaniciId);

        Integer etkilenenSatir = projeRepository.sil(safeFirmaId, safeProjeId, safeKullaniciId);

        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("Proje bulunamadı.");
        }
    }

    private Integer validateFirmaId(Integer firmaId) {
        if (firmaId == null || firmaId <= 0) {
            throw BusinessException.badRequest("Geçerli bir firma id giriniz.");
        }

        return firmaId;
    }

    private Integer validateProjeId(Integer projeId) {
        if (projeId == null || projeId <= 0) {
            throw BusinessException.badRequest("Geçerli bir proje id giriniz.");
        }

        return projeId;
    }

    private Integer validateKullaniciId(Integer kullaniciId) {
        if (kullaniciId == null || kullaniciId <= 0) {
            throw BusinessException.badRequest("Geçerli bir kullanıcı id giriniz.");
        }

        return kullaniciId;
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(limit, MAX_LIMIT);
    }

    private String normalizeDurum(String durum) {
        if (durum == null || durum.isBlank()) {
            return null;
        }

        String normalized = durum.trim().toUpperCase(Locale.ROOT);

        if (!PROJE_DURUMLARI.contains(normalized)) {
            throw BusinessException.badRequest("Geçersiz proje durumu.");
        }

        return normalized;
    }

    private String validateRequiredDurum(String durum) {
        String normalized = normalizeDurum(durum);

        if (normalized == null) {
            throw BusinessException.badRequest("Proje durumu zorunludur.");
        }

        return normalized;
    }

    private String validateProjeOkumaRolu(String rol) {
        if (rol == null || rol.isBlank()) {
            throw BusinessException.badRequest("Geçerli bir kullanıcı rolü bulunamadı.");
        }

        String normalized = rol.trim().toUpperCase(Locale.ROOT);

        if (!PROJE_OKUMA_ROLLERI.contains(normalized)) {
            throw BusinessException.forbidden("Bu işlem için yetkiniz yok.");
        }

        return normalized;
    }

    private Integer validateKullaniciIdIfRequired(Integer kullaniciId, String rol) {
        // Proje yoneticisi icin proje_kullanici tablosundan atama kontrolu yapilir.
        if ("PROJE_YONETICISI".equals(rol)) {
            return validateKullaniciId(kullaniciId);
        }

        return kullaniciId;
    }

    private BigDecimal normalizeButce(BigDecimal butce) {
        if (butce == null) {
            return BigDecimal.ZERO;
        }

        return butce;
    }

    private Integer normalizeButceUyariYuzde(Integer butceUyariYuzde) {
        if (butceUyariYuzde == null) {
            return DEFAULT_BUTCE_UYARI_YUZDE;
        }

        return butceUyariYuzde;
    }

    // Model nesnesini dis dunyaya donen response DTO'suna ceviriyoruz.
    private ProjeResponse toResponse(Proje proje) {
        return ProjeResponse.builder()
                .projeId(proje.getProjeId())
                .firmaId(proje.getFirmaId())
                .ad(proje.getAd())
                .aciklama(proje.getAciklama())
                .konum(proje.getKonum())
                .butce(proje.getButce())
                .baslangicTarihi(proje.getBaslangicTarihi())
                .bitisTarihi(proje.getBitisTarihi())
                .durum(proje.getDurum())
                .butceUyariYuzde(proje.getButceUyariYuzde())
                .toplamIsEmri(proje.getToplamIsEmri())
                .tamamlananIsEmri(proje.getTamamlananIsEmri())
                .taseronSayisi(proje.getTaseronSayisi())
                .toplamOdeme(proje.getToplamOdeme())
                .butceKullanimYuzdesi(proje.getButceKullanimYuzdesi())
                .createdAt(proje.getCreatedAt())
                .updatedAt(proje.getUpdatedAt())
                .build();
    }
}
