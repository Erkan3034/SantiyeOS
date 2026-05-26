package com.santiyeos.api.repository;

import com.santiyeos.api.model.Kullanici;
import com.santiyeos.api.model.PageResult;

public interface KullaniciRepository {

    PageResult<Kullanici> listele(Integer firmaId, String rol, Boolean aktif, int limit, int offset);

    Kullanici getir(Integer firmaId, Integer kullaniciId);

    Integer ekle(Integer firmaId, Kullanici kullanici);

    Integer guncelle(Integer firmaId, Integer kullaniciId, Kullanici kullanici);

    Integer sil(Integer firmaId, Integer kullaniciId);

    Integer sifreGuncelle(Integer kullaniciId, String sifreHash, Boolean sifreDegistirmeli);
}
