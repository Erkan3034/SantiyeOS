package com.santiyeos.api.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TaseronPerformansRaporu {

    private Integer taseronId;
    private String ad;
    private String uzmanlik;
    private BigDecimal performansSkoru;
    private Integer toplamIs;
    private Integer tamamlanan;
    private Integer iptal;
    private Integer geciken;
    private BigDecimal ortGecikmeGun;
    private BigDecimal odenmemisBakiye;
}
