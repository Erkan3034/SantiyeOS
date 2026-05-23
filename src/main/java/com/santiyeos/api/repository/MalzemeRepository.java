package com.santiyeos.api.repository;

import com.santiyeos.api.model.Malzeme;
import com.santiyeos.api.model.PageResult;

public interface MalzemeRepository {

    PageResult<Malzeme> listele(Integer firmaId, Integer kategoriId, Boolean kritikStok, int limit, int offset);

    Malzeme getir(Integer firmaId, Integer malzemeId);

    Integer ekle(Integer firmaId, String rol, Malzeme malzeme);

    Integer guncelle(Integer firmaId, Integer malzemeId, String rol, Malzeme malzeme);

    Integer sil(Integer firmaId, Integer malzemeId, Integer kullaniciId, String rol);
}
