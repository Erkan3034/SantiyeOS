package com.santiyeos.api.repository;

import com.santiyeos.api.model.Kullanici;

public interface AuthRepository {

    Kullanici emailIleGetir(String email);

    Kullanici idIleGetir(Integer kullaniciId);

    void sonGirisGuncelle(Integer kullaniciId);
}
