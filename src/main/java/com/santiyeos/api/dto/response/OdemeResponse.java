package com.santiyeos.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OdemeResponse {

    private Integer odemeId;
    private Integer firmaId;
    private Integer hakedisId;
    private Integer kaydedenId;

    private BigDecimal tutar;
    private LocalDate odemeTarihi;
    private String odemeYontemi;
    private String aciklama;
    private LocalDateTime createdAt;

    private BigDecimal hakedisTutari;
    private String hakedisOnayDurumu;
    private Integer isEmriId;
    private String isEmriBaslik;
    private Integer projeId;
    private String projeAd;
    private Integer taseronId;
    private String taseronAd;
    private String kaydeden;
    private BigDecimal toplamOdenen;
    private BigDecimal kalanTutar;
}
