package com.santiyeos.api.service.impl;

import com.santiyeos.api.dto.response.DashboardOzetResponse;
import com.santiyeos.api.exception.BusinessException;
import com.santiyeos.api.model.DashboardOzet;
import com.santiyeos.api.repository.DashboardRepository;
import com.santiyeos.api.security.Roles;
import com.santiyeos.api.service.DashboardService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Set<String> DASHBOARD_ROLLERI = Set.of(
            Roles.SUPER_ADMIN,
            Roles.FIRMA_ADMIN
    );

    private final DashboardRepository dashboardRepository;

    public DashboardServiceImpl(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @Override
    public DashboardOzetResponse ozet(Integer firmaId, String rol) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        validateRole(rol);

        DashboardOzet dashboardOzet = dashboardRepository.ozet(safeFirmaId);
        if (dashboardOzet == null) {
            throw BusinessException.notFound("Dashboard ozeti bulunamadi.");
        }

        return toResponse(dashboardOzet);
    }

    private Integer validateFirmaId(Integer firmaId) {
        if (firmaId == null || firmaId <= 0) {
            throw BusinessException.badRequest("Gecerli bir firma id giriniz.");
        }

        return firmaId;
    }

    private String validateRole(String rol) {
        if (rol == null || rol.isBlank()) {
            throw BusinessException.badRequest("Gecerli bir kullanici rolu bulunamadi.");
        }

        String normalized = rol.trim().toUpperCase(Locale.ROOT);
        if (!DASHBOARD_ROLLERI.contains(normalized)) {
            throw BusinessException.forbidden("Bu islem icin yetkiniz yok.");
        }

        return normalized;
    }

    private DashboardOzetResponse toResponse(DashboardOzet dashboardOzet) {
        return DashboardOzetResponse.builder()
                .genelOzet(toResponse(dashboardOzet.getGenelOzet()))
                .isEmriDurumlari(toDurumResponses(dashboardOzet.getIsEmriDurumlari()))
                .hakedisDurumlari(toDurumResponses(dashboardOzet.getHakedisDurumlari()))
                .finansOzet(toResponse(dashboardOzet.getFinansOzet()))
                .kritikStoklar(toKritikStokResponses(dashboardOzet.getKritikStoklar()))
                .yaklasanProjeler(toYaklasanProjeResponses(dashboardOzet.getYaklasanProjeler()))
                .build();
    }

    private DashboardOzetResponse.GenelOzet toResponse(DashboardOzet.GenelOzet genelOzet) {
        if (genelOzet == null) {
            return null;
        }

        return DashboardOzetResponse.GenelOzet.builder()
                .aktifProje(genelOzet.getAktifProje())
                .aktifIsEmri(genelOzet.getAktifIsEmri())
                .gecikenIsEmri(genelOzet.getGecikenIsEmri())
                .bekleyenHakedis(genelOzet.getBekleyenHakedis())
                .kritikStokSayisi(genelOzet.getKritikStokSayisi())
                .build();
    }

    private DashboardOzetResponse.FinansOzet toResponse(DashboardOzet.FinansOzet finansOzet) {
        if (finansOzet == null) {
            return null;
        }

        return DashboardOzetResponse.FinansOzet.builder()
                .toplamOnaylananHakedis(finansOzet.getToplamOnaylananHakedis())
                .toplamOdeme(finansOzet.getToplamOdeme())
                .toplamOdenmemis(finansOzet.getToplamOdenmemis())
                .bekleyenHakedis(finansOzet.getBekleyenHakedis())
                .onaylananHakedis(finansOzet.getOnaylananHakedis())
                .build();
    }

    private List<DashboardOzetResponse.DurumDagilimi> toDurumResponses(List<DashboardOzet.DurumDagilimi> durumlar) {
        if (durumlar == null) {
            return List.of();
        }

        return durumlar.stream()
                .map(durum -> DashboardOzetResponse.DurumDagilimi.builder()
                        .durum(durum.getDurum())
                        .toplam(durum.getToplam())
                        .build())
                .toList();
    }

    private List<DashboardOzetResponse.KritikStok> toKritikStokResponses(List<DashboardOzet.KritikStok> stoklar) {
        if (stoklar == null) {
            return List.of();
        }

        return stoklar.stream()
                .map(stok -> DashboardOzetResponse.KritikStok.builder()
                        .malzemeId(stok.getMalzemeId())
                        .ad(stok.getAd())
                        .kategoriAd(stok.getKategoriAd())
                        .birim(stok.getBirim())
                        .stokMiktari(stok.getStokMiktari())
                        .minStok(stok.getMinStok())
                        .build())
                .toList();
    }

    private List<DashboardOzetResponse.YaklasanProje> toYaklasanProjeResponses(List<DashboardOzet.YaklasanProje> projeler) {
        if (projeler == null) {
            return List.of();
        }

        return projeler.stream()
                .map(proje -> DashboardOzetResponse.YaklasanProje.builder()
                        .projeId(proje.getProjeId())
                        .ad(proje.getAd())
                        .bitisTarihi(proje.getBitisTarihi())
                        .durum(proje.getDurum())
                        .kalanGun(proje.getKalanGun())
                        .build())
                .toList();
    }
}
