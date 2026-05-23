package com.santiyeos.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateMalzemeKategoriRequest {

    @NotBlank(message = "Kategori adi zorunludur")
    @Size(max = 100, message = "Kategori adi en fazla 100 karakter olabilir")
    private String ad;

    @Size(max = 300, message = "Aciklama en fazla 300 karakter olabilir")
    private String aciklama;
}
