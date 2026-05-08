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
public class ProjeResponse {

    private Integer projeId;
    private Integer firmaId;

    private String ad;
    private String aciklama;
    private String konum;
    private BigDecimal butce;

    private LocalDate baslangicTarihi;
    private LocalDate bitisTarihi;

    private String durum;
    private Integer butceUyariYuzde;

    private Integer toplamIsEmri;
    private Integer tamamlananIsEmri;
    private Integer taseronSayisi;
    private BigDecimal toplamOdeme;
    private BigDecimal butceKullanimYuzdesi;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
