package com.santiyeos.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateIsEmriDurumRequest {

    @NotBlank(message = "Durum zorunludur")
    @Pattern(
            regexp = "BEKLIYOR|BASLADI|DEVAM_EDIYOR|TAMAMLANDI|IPTAL",
            message = "Geçersiz iş emri durumu"
    )
    private String durum;

    @Size(max = 500, message = "Durum açıklaması en fazla 500 karakter olabilir")
    private String aciklama;
}
