package com.santiyeos.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public final class LookupResponse {
    private LookupResponse() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Proje {
        private Integer projeId;
        private String ad;
        private String durum;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Taseron {
        private Integer taseronId;
        private String ad;
        private String uzmanlik;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Kullanici {
        private Integer kullaniciId;
        private Integer taseronId;
        private String ad;
        private String soyad;
        private String email;
        private String rol;
        private String taseronAd;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Malzeme {
        private Integer malzemeId;
        private Integer kategoriId;
        private String ad;
        private String birim;
        private String kategoriAd;
        private BigDecimal stokMiktari;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AbonelikPlan {
        private Integer planId;
        private String ad;
        private Integer maxProje;
        private Integer maxKullanici;
        private Integer maxTaseron;
        private BigDecimal aylikUcret;
    }
}
