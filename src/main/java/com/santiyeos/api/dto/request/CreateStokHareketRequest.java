package com.santiyeos.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateStokHareketRequest {

    @NotNull(message = "Malzeme id zorunludur")
    @Positive(message = "Gecerli bir malzeme id giriniz")
    private Integer malzemeId;

    @Positive(message = "Gecerli bir proje id giriniz")
    private Integer projeId;

    @Positive(message = "Gecerli bir is emri id giriniz")
    private Integer isEmriId;

    @NotNull(message = "Hareket tipi zorunludur")
    @Pattern(regexp = "GIRIS|CIKIS", message = "Gecersiz stok hareket tipi")
    private String hareketTipi;

    @NotNull(message = "Miktar zorunludur")
    @DecimalMin(value = "0.01", message = "Miktar sifirdan buyuk olmalidir")
    private BigDecimal miktar;

    @DecimalMin(value = "0.00", message = "Birim fiyat negatif olamaz")
    private BigDecimal birimFiyat;

    @Size(max = 5000, message = "Aciklama en fazla 5000 karakter olabilir")
    private String aciklama;
}
