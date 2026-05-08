package com.santiyeos.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateProjeRequest {

    @NotBlank(message = "Proje adı zorunludur")
    @Size(max = 200, message = "Proje adı en fazla 200 karakter olabilir")
    private String ad;

    @Size(max = 5000, message = "Açıklama en fazla 5000 karakter olabilir")
    private String aciklama;

    @Size(max = 300, message = "Konum en fazla 300 karakter olabilir")
    private String konum;

    @DecimalMin(value = "0.00", message = "Bütçe negatif olamaz")
    private BigDecimal butce;

    private LocalDate baslangicTarihi;
    private LocalDate bitisTarihi;

    @Min(value = 1, message = "Bütçe uyarı yüzdesi en az 1 olmalıdır")
    @Max(value = 99, message = "Bütçe uyarı yüzdesi en fazla 99 olabilir")
    private Integer butceUyariYuzde;
}
