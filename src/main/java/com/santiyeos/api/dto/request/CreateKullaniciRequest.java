package com.santiyeos.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.apache.catalina.authenticator.SavedRequest;

@Data
public class CreateKullaniciRequest {
    // yeni kullanici olusurmak icin gelen gerekli veriler

    private Integer taseronId;

    @NotBlank(message = "Ad zorunludur")
    @Size(max=100, message = "Ad en fazla 100 karakter olabilir")
    private  String ad;

    @NotBlank(message = "Soyad zorunludur")
    @Size(max = 100, message = "Soyad en fazla 100 karakter olabilir")
    private String soyad;

    @NotBlank(message = "E-posta zorunludur!")
    @Email(message = "Geçerli bir e-posta giriniz.")
    @Size(max=150, message = "E-posta en fazla 150 karakter olabilir.")
    private String email;

    @NotBlank(message = "Şifre zorunludur")
    @Size(min = 6, max = 72, message = "Şifre 6-72 karakter arasında olmalıdır")
    private String sifre;

    @NotBlank(message = "Rol zorunludur")
    @Pattern(
            regexp = "SUPER_ADMIN|FIRMA_ADMIN|PROJE_YONETICISI|SAHA_PERSONELI|TASERON_TEMSILCI",
            message = "Geçersiz kullanıcı rolü"
    )
    private String rol;

    @Size(max =20 , message = "Telefon numarası en fazla 20 karkater olabilir.")
    private String telefon;

}
