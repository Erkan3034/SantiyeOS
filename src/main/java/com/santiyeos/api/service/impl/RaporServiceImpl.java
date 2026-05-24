package com.santiyeos.api.service.impl;

import com.santiyeos.api.dto.response.GenelOzetRaporuResponse;
import com.santiyeos.api.dto.response.ProjeMaliyetRaporuResponse;
import com.santiyeos.api.dto.response.TaseronPerformansRaporuResponse;
import com.santiyeos.api.exception.BusinessException;
import com.santiyeos.api.model.GenelOzetRaporu;
import com.santiyeos.api.model.ProjeMaliyetRaporu;
import com.santiyeos.api.model.TaseronPerformansRaporu;
import com.santiyeos.api.repository.RaporRepository;
import com.santiyeos.api.security.Roles;
import com.santiyeos.api.service.RaporService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class RaporServiceImpl implements RaporService {

    private static final Set<String> GENEL_OZET_ROLLERI = Set.of(
            Roles.SUPER_ADMIN,
            Roles.FIRMA_ADMIN
    );

    private static final Set<String> PROJE_RAPOR_ROLLERI = Set.of(
            Roles.SUPER_ADMIN,
            Roles.FIRMA_ADMIN,
            Roles.PROJE_YONETICISI
    );

    private final RaporRepository raporRepository;

    public RaporServiceImpl(RaporRepository raporRepository) {
        this.raporRepository = raporRepository;
    }

    @Override
    public GenelOzetRaporuResponse genelOzet(Integer firmaId) {
        Integer safeFirmaId = validateFirmaId(firmaId);

        GenelOzetRaporu rapor = raporRepository.genelOzet(safeFirmaId);
        if (rapor == null) {
            throw BusinessException.notFound("Rapor bulunamadi.");
        }

        return toResponse(rapor);
    }

    @Override
    public ProjeMaliyetRaporuResponse projeMaliyet(Integer firmaId, Integer kullaniciId, String rol, Integer projeId) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeKullaniciId = validateKullaniciId(kullaniciId);
        Integer safeProjeId = validatePositive(projeId, "Gecerli bir proje id giriniz.");
        String safeRol = validateRole(rol, PROJE_RAPOR_ROLLERI);

        ProjeMaliyetRaporu rapor = raporRepository.projeMaliyet(safeFirmaId, safeKullaniciId, safeRol, safeProjeId);
        if (rapor == null) {
            throw BusinessException.notFound("Proje maliyet raporu bulunamadi.");
        }

        return toResponse(rapor);
    }

    @Override
    public List<TaseronPerformansRaporuResponse> taseronPerformans(Integer firmaId, Integer kullaniciId, String rol) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeKullaniciId = validateKullaniciId(kullaniciId);
        String safeRol = validateRole(rol, PROJE_RAPOR_ROLLERI);

        return raporRepository.taseronPerformans(safeFirmaId, safeKullaniciId, safeRol)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void genelOzetRoluKontrolEt(String rol) {
        validateRole(rol, GENEL_OZET_ROLLERI);
    }

    private Integer validateFirmaId(Integer firmaId) {
        return validatePositive(firmaId, "Gecerli bir firma id giriniz.");
    }

    private Integer validateKullaniciId(Integer kullaniciId) {
        return validatePositive(kullaniciId, "Gecerli bir kullanici id giriniz.");
    }

    private Integer validatePositive(Integer value, String message) {
        if (value == null || value <= 0) {
            throw BusinessException.badRequest(message);
        }

        return value;
    }

    private String validateRole(String rol, Set<String> allowedRoles) {
        if (rol == null || rol.isBlank()) {
            throw BusinessException.badRequest("Gecerli bir kullanici rolu bulunamadi.");
        }

        String normalized = rol.trim().toUpperCase(Locale.ROOT);
        if (!allowedRoles.contains(normalized)) {
            throw BusinessException.forbidden("Bu islem icin yetkiniz yok.");
        }

        return normalized;
    }

    private GenelOzetRaporuResponse toResponse(GenelOzetRaporu rapor) {
        return GenelOzetRaporuResponse.builder()
                .aktifProje(rapor.getAktifProje())
                .aktifIsEmri(rapor.getAktifIsEmri())
                .gecikenIsEmri(rapor.getGecikenIsEmri())
                .bekleyenHakedis(rapor.getBekleyenHakedis())
                .toplamOdenmemis(rapor.getToplamOdenmemis())
                .kritikStokSayisi(rapor.getKritikStokSayisi())
                .build();
    }

    private ProjeMaliyetRaporuResponse toResponse(ProjeMaliyetRaporu rapor) {
        return ProjeMaliyetRaporuResponse.builder()
                .projeId(rapor.getProjeId())
                .projeAd(rapor.getProjeAd())
                .toplamButce(rapor.getToplamButce())
                .durum(rapor.getDurum())
                .isEmriSayisi(rapor.getIsEmriSayisi())
                .taseronSayisi(rapor.getTaseronSayisi())
                .toplamOnaylananHakedis(rapor.getToplamOnaylananHakedis())
                .toplamOdeme(rapor.getToplamOdeme())
                .kalanButce(rapor.getKalanButce())
                .butceKullanimYuzdesi(rapor.getButceKullanimYuzdesi())
                .build();
    }

    private TaseronPerformansRaporuResponse toResponse(TaseronPerformansRaporu rapor) {
        return TaseronPerformansRaporuResponse.builder()
                .taseronId(rapor.getTaseronId())
                .ad(rapor.getAd())
                .uzmanlik(rapor.getUzmanlik())
                .performansSkoru(rapor.getPerformansSkoru())
                .toplamIs(rapor.getToplamIs())
                .tamamlanan(rapor.getTamamlanan())
                .iptal(rapor.getIptal())
                .geciken(rapor.getGeciken())
                .ortGecikmeGun(rapor.getOrtGecikmeGun())
                .odenmemisBakiye(rapor.getOdenmemisBakiye())
                .build();
    }
}
