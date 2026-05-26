package com.santiyeos.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AssignProjeKullaniciRequest {

    @NotNull(message = "Kullanici id zorunludur")
    @Positive(message = "Gecerli bir kullanici id giriniz")
    private Integer kullaniciId;
}
