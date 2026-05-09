package com.santiyeos.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IsEmriResponse {

    private Integer isEmriId;
    private Integer firmaId;
    private Integer projeId;
    private Integer taseronId;
    private Integer atananKullaniciId;
    private Integer olusturanId;

    private String baslik;
    private String aciklama;
    private String oncelik;
    private String durum;

    private LocalDate baslangicTarihi;
    private LocalDate bitisTarihi;
    private LocalDateTime tamamlanmaTarihi;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String projeAd;
    private String taseronAd;
    private String taseronUzmanlik;
    private String atananKullanici;
    private String olusturan;

    private Integer notSayisi;
    private Integer raporSayisi;
    private Integer kalanGun;
}
