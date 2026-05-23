package com.santiyeos.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateBildirimRequest {

    @NotNull(message = "Kullanici id zorunludur")
    @Positive(message = "Gecerli bir kullanici id giriniz")
    private Integer kullaniciId;

    @NotBlank(message = "Baslik zorunludur")
    @Size(max = 200, message = "Baslik en fazla 200 karakter olabilir")
    private String baslik;

    @NotBlank(message = "Mesaj zorunludur")
    private String mesaj;

    @NotBlank(message = "Bildirim tipi zorunludur")
    @Pattern(
            regexp = "IS_EMRI|HAKEDIS|ODEME|BUTCE|SISTEM",
            message = "Gecersiz bildirim tipi"
    )
    private String tip;

    @Size(max = 50, message = "Referans tablo en fazla 50 karakter olabilir")
    private String referansTablo;

    @Positive(message = "Referans id pozitif olmalidir")
    private Integer referansId;
}
