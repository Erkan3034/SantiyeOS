package com.santiyeos.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateTaseronRequest {
    @NotBlank(message = "Taşeron Adı Zorunludur!")
    @Size(max= 200 , message = "Taşeron adı en fazla 200 karakter olabilir.")
    private String ad;

    @Size(max = 20 , message = "Vergi No en fazla 20 karakter olabilir.")
    private String vergiNo;

    @Size(max = 200 , message ="Yetkili adı en fazla 200 karakter olabilir.")
    private String yetkiliAd;

    @Size(max = 20, message = "Telefon en fazla 20 karakter olabilir")
    private String telefon;

    @Email(message = "Geçerli bir email giriniz")
    @Size(max = 150, message = "Email en fazla 150 karakter olabilir")
    private String email;

    @Size(max = 200, message = "Uzmanlık en fazla 200 karakter olabilir")
    private String uzmanlik;


    // getter  - setter (lombok kullanilmadi)
    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public String getVergiNo() {
        return vergiNo;
    }

    public void setVergiNo(String vergiNo) {
        this.vergiNo = vergiNo;
    }

    public String getYetkiliAd() {
        return yetkiliAd;
    }

    public void setYetkiliAd(String yetkiliAd) {
        this.yetkiliAd = yetkiliAd;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUzmanlik() {
        return uzmanlik;
    }

    public void setUzmanlik(String uzmanlik) {
        this.uzmanlik = uzmanlik;
    }

}
