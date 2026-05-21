package com.santiyeos.api.repository;

import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.model.Proje;

public interface ProjeRepository {

    // Repository katmanı DB ile konuşur; iş kuralları Service katmanında kalır.
    PageResult<Proje> listele(Integer firmaId, Integer kullaniciId, String rol, String durum, int limit, int offset);

    Proje getir(Integer firmaId, Integer kullaniciId, String rol, Integer projeId);

    Integer ekle(Integer firmaId, Proje proje);

    Integer guncelle(Integer firmaId, Integer projeId, Proje proje);

    Integer sil(Integer firmaId, Integer projeId, Integer kullaniciId);
}
