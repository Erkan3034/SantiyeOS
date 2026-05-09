package com.santiyeos.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateHakedisRequest {

    @NotNull(message = "İş emri id zorunludur")
    @Positive(message = "Geçerli bir iş emri id giriniz")
    private Integer isEmriId;

    @NotNull(message = "Hakediş tutarı zorunludur")
    @DecimalMin(value = "0.01", message = "Hakediş tutarı sıfırdan büyük olmalıdır")
    private BigDecimal tutar;

    @Size(max = 5000, message = "Açıklama en fazla 5000 karakter olabilir")
    private String aciklama;
}
