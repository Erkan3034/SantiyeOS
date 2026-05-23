package com.santiyeos.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class StokHareketResponse {

    private Integer hareketId;
    private Integer firmaId;
    private Integer malzemeId;
    private Integer projeId;
    private Integer isEmriId;
    private Integer kaydedenId;
    private String hareketTipi;
    private BigDecimal miktar;
    private BigDecimal birimFiyat;
    private String aciklama;
    private LocalDateTime createdAt;

    private String malzemeAd;
    private String birim;
    private String projeAd;
    private String isEmriBaslik;
    private String kaydeden;
}
