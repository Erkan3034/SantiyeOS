package com.santiyeos.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateIsEmriRequest {

    @Positive(message = "Geçerli bir atanan kullanıcı id giriniz")
    private Integer atananKullaniciId;

    @NotBlank(message = "İş emri başlığı zorunludur")
    @Size(max = 300, message = "İş emri başlığı en fazla 300 karakter olabilir")
    private String baslik;

    @Size(max = 5000, message = "Açıklama en fazla 5000 karakter olabilir")
    private String aciklama;

    @NotBlank(message = "Öncelik zorunludur")
    @Pattern(
            regexp = "DUSUK|NORMAL|YUKSEK|KRITIK",
            message = "Geçersiz iş emri önceliği"
    )
    private String oncelik;

    private LocalDate baslangicTarihi;
    private LocalDate bitisTarihi;
}
