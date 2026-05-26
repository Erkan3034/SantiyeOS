package com.santiyeos.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProjeKullaniciResponse {
    private Integer projeId;
    private Integer kullaniciId;
    private Integer firmaId;
    private String ad;
    private String soyad;
    private String rol;
    private String email;
    private LocalDateTime atanmaTarihi;
}
