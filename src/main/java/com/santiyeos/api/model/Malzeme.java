package com.santiyeos.api.model;

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
public class Malzeme {

    private Integer malzemeId;
    private Integer firmaId;
    private Integer kategoriId;
    private String ad;
    private String birim;
    private BigDecimal birimFiyat;
    private BigDecimal stokMiktari;
    private BigDecimal minStok;
    private Boolean kritikStok;
    private Boolean aktif;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String kategoriAd;
}
