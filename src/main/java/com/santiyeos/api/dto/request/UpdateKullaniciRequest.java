package com.santiyeos.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateKullaniciRequest {

    private Integer taseronId;

    @NotBlank(message = "Ad zorunludur")
    @Size(max = 100, message = "Ad en fazla 100 karakter olabilir")
    private String ad;

    @NotBlank(message = "Soyad zorunludur")
    @Size(max = 100, message = "Soyad en fazla 100 karakter olabilir")
    private String soyad;

    @NotBlank(message = "E-posta zorunludur")
    @Email(message = "Geçerli bir e-posta giriniz")
    @Size(max = 150, message = "E-posta en fazla 150 karakter olabilir")
    private String email;

    @Size(max = 20, message = "Telefon en fazla 20 karakter olabilir")
    private String telefon;

    @NotBlank(message = "Rol zorunludur")
    @Pattern(
            regexp = "SUPER_ADMIN|FIRMA_ADMIN|PROJE_YONETICISI|SAHA_PERSONELI|TASERON_TEMSILCI",
            message = "Geçersiz kullanıcı rolü"
    )
    private String rol;

    @NotNull(message = "Aktif bilgisi zorunludur")
    private Boolean aktif;
}