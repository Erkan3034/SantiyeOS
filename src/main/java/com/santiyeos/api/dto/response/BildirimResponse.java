package com.santiyeos.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BildirimResponse {

    private Integer bildirimId;
    private Integer firmaId;
    private Integer kullaniciId;
    private String baslik;
    private String mesaj;
    private String tip;
    private String referansTablo;
    private Integer referansId;
    private Boolean okundu;
    private LocalDateTime okunduTarihi;
    private LocalDateTime createdAt;
}
