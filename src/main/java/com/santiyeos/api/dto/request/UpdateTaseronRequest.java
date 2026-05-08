package com.santiyeos.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateTaseronRequest {

    //PUT istegınde taseronun ana bilgilerini tam guncelliyoruz.
    @NotBlank(message = "Taseron adı zorunludur")
    @Size(max = 200, message = "Taseron adı en fazla 200 karakter olabilir")
    private String ad;


    @Size(max = 20, message = "Vergi no en fazla 20 karakter olabilir.")
    private  String vergiNo;

    @Size(max = 200, message = "Yetkili adı en fazla 200 karakter olabilir")
    private String yetkiliAd;

    @Size(max = 20, message = "Telefon en fazla 20 karakter olabilir")
    private String telefon;

    @Email(message = "Geçerli bir email giriniz")
    @Size(max = 150, message = "Email en fazla 150 karakter olabilir")
    private String email;

    @Size(max = 200, message = "Uzmanlık en fazla 200 karakter olabilir")
    private String uzmanlik;
}
