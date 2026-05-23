package com.santiyeos.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MalzemeKategoriResponse {

    private Integer kategoriId;
    private Integer firmaId;
    private String ad;
    private String aciklama;
    private Boolean aktif;
    private LocalDateTime createdAt;
    private Integer malzemeSayisi;
}
