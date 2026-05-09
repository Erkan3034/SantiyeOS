package com.santiyeos.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthKullaniciResponse {

    private Integer kullaniciId;
    private Integer firmaId;
    private Integer taseronId;
    private String ad;
    private String soyad;
    private String email;
    private String rol;
    private String telefon;
}
