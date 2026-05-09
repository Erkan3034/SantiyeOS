package com.santiyeos.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HakedisResponse {

    private Integer hakedisId;
    private Integer firmaId;
    private Integer isEmriId;
    private Integer talepEdenId;
    private Integer onaylayanId;

    private BigDecimal tutar;
    private String onayDurumu;
    private LocalDateTime onayTarihi;
    private String aciklama;
    private String redGerekce;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Integer projeId;
    private String projeAd;
    private Integer taseronId;
    private String taseronAd;
    private String isEmriBaslik;
    private LocalDateTime tamamlanmaTarihi;
    private String talepEden;
    private String onaylayan;
    private BigDecimal odenenTutar;
    private BigDecimal kalanTutar;
}
