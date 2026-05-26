package com.santiyeos.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class KullaniciResponse {
    private Integer kullaniciId;
    private Integer firmaId;
    private Integer taseronId;
    private String ad;
    private String soyad;
    private String email;
    private String rol;
    private String telefon;
    private Boolean aktif;
    private Boolean sifreDegistirmeli;
    private LocalDateTime sonGiris;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String firmaAd;
    private String taseronAd;
}


// response da şifre hash donmemeli
