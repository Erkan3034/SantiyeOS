package com.santiyeos.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProjeMaliyetRaporuResponse {

    private Integer projeId;
    private String projeAd;
    private BigDecimal toplamButce;
    private String durum;
    private Integer isEmriSayisi;
    private Integer taseronSayisi;
    private BigDecimal toplamOnaylananHakedis;
    private BigDecimal toplamOdeme;
    private BigDecimal kalanButce;
    private BigDecimal butceKullanimYuzdesi;
}
