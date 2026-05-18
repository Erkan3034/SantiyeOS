package com.santiyeos.api.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class ResetKullaniciSifreRequest {
    @NotBlank(message = "Yeni şifre zorunludur.")
    @Size(min =6, max=72, message = "Şifre 6-72 karakter arasında olmaldır.")
    private String yeniSifre;
}
