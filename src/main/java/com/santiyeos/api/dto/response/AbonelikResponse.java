package com.santiyeos.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AbonelikResponse {

    private Integer abonelikId;
    private Integer firmaId;
    private String firmaAd;
    private Integer planId;
    private String planAd;
    private Integer maxProje;
    private Integer maxKullanici;
    private Integer maxTaseron;
    private BigDecimal aylikUcret;
    private LocalDate baslangicTarihi;
    private LocalDate bitisTarihi;
    private String durum;
    private Boolean deneme;
    private LocalDateTime createdAt;
}