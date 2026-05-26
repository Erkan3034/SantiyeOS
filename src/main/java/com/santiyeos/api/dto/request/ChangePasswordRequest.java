package com.santiyeos.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "Mevcut sifre zorunludur.")
    private String mevcutSifre;

    @NotBlank(message = "Yeni sifre zorunludur.")
    @Size(min = 6, max = 72, message = "Yeni sifre 6-72 karakter arasinda olmalidir.")
    private String yeniSifre;
}
