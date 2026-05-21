package com.santiyeos.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AbonelikPlanResponse {

    private Integer planId;
    private String ad;
    private Integer maxProje;
    private Integer maxKullanici;
    private Integer maxTaseron;
    private BigDecimal aylikUcret;
    private Boolean aktif;
}

// apinin dışarı dondugu guvenli cevap