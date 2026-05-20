package com.santiyeos.api.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kullanici {
    private Integer kullaniciId;
    private Integer firmaId;
    private Integer taseronId;
    private String ad;
    private String soyad;
    private String email;
    private String sifreHash;
    private String rol;
    private String telefon;
    private Boolean aktif;
    private LocalDateTime sonGiris;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String firmaAd;
    private String taseronAd;
}