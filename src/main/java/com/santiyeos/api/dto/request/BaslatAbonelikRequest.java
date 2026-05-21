package com.santiyeos.api.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BaslatAbonelikRequest {

    @NotNull(message = "Plan bilgisi zorunludur")
    private Integer planId;

    @NotNull(message = "Başlangıç tarihi zorunludur")
    private LocalDate baslangicTarihi;

    @NotNull(message = "Bitiş tarihi zorunludur")
    @Future(message = "Bitiş tarihi gelecekte olmalıdır")
    private LocalDate bitisTarihi;

    private Boolean deneme;
}