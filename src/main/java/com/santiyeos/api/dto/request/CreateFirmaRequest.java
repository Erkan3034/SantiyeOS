package com.santiyeos.api.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;


@Data
public class CreateFirmaRequest {

    @NotBlank(message = "Firma adı zorunludur.")
    @Size(max=200, message ="Firma adı en fazla 200 karkater olabilir." )
    private String ad;

    @NotBlank(message = "Vergi numarası zorunludur")
    @Size(max = 20, message = "Vergi numarası en fazla 20 karakter olabilir")
    private String vergiNo;

    @Size(max = 20, message = "Telefon en fazla 20 karakter olabilir")
    private String telefon;

    @NotBlank(message = "E-posta zorunludur")
    @Email(message = "Geçerli bir e-posta giriniz")
    @Size(max = 150, message = "E-posta en fazla 150 karakter olabilir")
    private String email;

    private String adres;

}
