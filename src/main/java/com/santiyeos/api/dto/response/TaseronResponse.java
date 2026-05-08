package com.santiyeos.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data // create get - set automatically.
@NoArgsConstructor // create empty constructor
@AllArgsConstructor // create constructor thathave all fields
@Builder // create object more readable
public class TaseronResponse {

    private Integer taseronId;
    private Integer firmaId;
    private String ad;
    private String vergiNo;
    private String yetkiliAd;
    private String telefon;
    private String email;
    private String uzmanlik;
    private BigDecimal performansSkoru;
    private Boolean aktif;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}