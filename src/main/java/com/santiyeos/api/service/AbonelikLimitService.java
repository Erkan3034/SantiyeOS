package com.santiyeos.api.service;

public interface AbonelikLimitService {

    void projeEklemeHakkiKontrolEt(Integer firmaId);

    void kullaniciEklemeHakkiKontrolEt(Integer firmaId);

    void taseronEklemeHakkiKontrolEt(Integer firmaId);
}