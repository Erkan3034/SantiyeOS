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
public class IsEmriRapor {

    private Integer raporId;
    private Integer firmaId;
    private Integer isEmriId;
    private Integer kullaniciId;
    private String baslik;
    private String icerik;
    private LocalDateTime createdAt;
    private String yazan;
    private String rol;
}
