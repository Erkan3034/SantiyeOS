package com.santiyeos.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateOdemeRequest {

    @NotNull(message = "Hakediş id zorunludur")
    @Positive(message = "Geçerli bir hakediş id giriniz")
    private Integer hakedisId;

    @NotNull(message = "Ödeme tutarı zorunludur")
    @DecimalMin(value = "0.01", message = "Ödeme tutarı sıfırdan büyük olmalıdır")
    private BigDecimal tutar;

    private LocalDate odemeTarihi;

    @Pattern(
            regexp = "HAVALE|EFT|CEK|NAKIT",
            message = "Geçersiz ödeme yöntemi"
    )
    private String odemeYontemi;

    @Size(max = 5000, message = "Açıklama en fazla 5000 karakter olabilir")
    private String aciklama;
}
