package com.santiyeos.api.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AbonelikPlan {

    private Integer planId;
    private String ad;
    private Integer maxProje;
    private Integer maxKullanici;
    private Integer maxTaseron;
    private BigDecimal aylikUcret;
    private Boolean aktif;
}

// db den gelen domain model