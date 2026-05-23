package com.santiyeos.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateMalzemeRequest {

    @Positive(message = "Gecerli bir kategori id giriniz")
    private Integer kategoriId;

    @NotBlank(message = "Malzeme adi zorunludur")
    @Size(max = 200, message = "Malzeme adi en fazla 200 karakter olabilir")
    private String ad;

    @Pattern(regexp = "ADET|KG|TON|METRE|M2|M3|LITRE", message = "Gecersiz malzeme birimi")
    private String birim;

    @DecimalMin(value = "0.00", message = "Birim fiyat negatif olamaz")
    private BigDecimal birimFiyat;

    @DecimalMin(value = "0.00", message = "Minimum stok negatif olamaz")
    private BigDecimal minStok;
}
