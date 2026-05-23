package com.santiyeos.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateIsEmriRaporRequest {

    @NotBlank(message = "Rapor basligi zorunludur")
    @Size(max = 200, message = "Rapor basligi en fazla 200 karakter olabilir")
    private String baslik;

    @NotBlank(message = "Rapor icerigi zorunludur")
    @Size(max = 10000, message = "Rapor icerigi en fazla 10000 karakter olabilir")
    private String icerik;
}
