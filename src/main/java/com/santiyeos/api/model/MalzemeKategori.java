package com.santiyeos.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MalzemeKategori {

    private Integer kategoriId;
    private Integer firmaId;
    private String ad;
    private String aciklama;
    private Boolean aktif;
    private LocalDateTime createdAt;
    private Integer malzemeSayisi;
}
