package com.santiyeos.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder // create constructor that have all fields
public class FirmaResponse {

    private Integer firmaId;
    private String ad;
    private String vergiNo;
    private String telefon;
    private String email;
    private String adres;
    private Boolean aktif;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

