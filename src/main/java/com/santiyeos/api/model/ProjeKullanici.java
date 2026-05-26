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
public class ProjeKullanici {
    private Integer projeId;
    private Integer kullaniciId;
    private Integer firmaId;
    private String ad;
    private String soyad;
    private String rol;
    private String email;
    private LocalDateTime atanmaTarihi;
}
