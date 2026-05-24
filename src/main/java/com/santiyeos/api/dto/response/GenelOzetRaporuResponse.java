package com.santiyeos.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class GenelOzetRaporuResponse {

    private Integer aktifProje;
    private Integer aktifIsEmri;
    private Integer gecikenIsEmri;
    private Integer bekleyenHakedis;
    private BigDecimal toplamOdenmemis;
    private Integer kritikStokSayisi;
}
