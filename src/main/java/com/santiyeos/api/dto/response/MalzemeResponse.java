package com.santiyeos.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class MalzemeResponse {

    private Integer malzemeId;
    private Integer firmaId;
    private Integer kategoriId;
    private String kategoriAd;
    private String ad;
    private String birim;
    private BigDecimal birimFiyat;
    private BigDecimal stokMiktari;
    private BigDecimal minStok;
    private Boolean kritikStok;
    private Boolean aktif;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
