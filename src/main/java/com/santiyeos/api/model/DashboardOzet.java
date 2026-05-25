package com.santiyeos.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardOzet {

    private GenelOzet genelOzet;
    private List<DurumDagilimi> isEmriDurumlari;
    private List<DurumDagilimi> hakedisDurumlari;
    private FinansOzet finansOzet;
    private List<KritikStok> kritikStoklar;
    private List<YaklasanProje> yaklasanProjeler;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GenelOzet {
        private Integer aktifProje;
        private Integer aktifIsEmri;
        private Integer gecikenIsEmri;
        private Integer bekleyenHakedis;
        private Integer kritikStokSayisi;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DurumDagilimi {
        private String durum;
        private Integer toplam;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FinansOzet {
        private BigDecimal toplamOnaylananHakedis;
        private BigDecimal toplamOdeme;
        private BigDecimal toplamOdenmemis;
        private Integer bekleyenHakedis;
        private Integer onaylananHakedis;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class KritikStok {
        private Integer malzemeId;
        private String ad;
        private String kategoriAd;
        private String birim;
        private BigDecimal stokMiktari;
        private BigDecimal minStok;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class YaklasanProje {
        private Integer projeId;
        private String ad;
        private LocalDate bitisTarihi;
        private String durum;
        private Integer kalanGun;
    }
}
