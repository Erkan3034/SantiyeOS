package com.santiyeos.api.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class GenelOzetRaporu {

    private Integer aktifProje;
    private Integer aktifIsEmri;
    private Integer gecikenIsEmri;
    private Integer bekleyenHakedis;
    private BigDecimal toplamOdenmemis;
    private Integer kritikStokSayisi;
}
