package com.santiyeos.api.repository;

import com.santiyeos.api.model.MalzemeKategori;
import com.santiyeos.api.model.PageResult;

public interface MalzemeKategoriRepository {

    PageResult<MalzemeKategori> listele(Integer firmaId, int limit, int offset);

    MalzemeKategori getir(Integer firmaId, Integer kategoriId);

    Integer ekle(Integer firmaId, String rol, MalzemeKategori kategori);

    Integer guncelle(Integer firmaId, Integer kategoriId, String rol, MalzemeKategori kategori);

    Integer sil(Integer firmaId, Integer kategoriId, Integer kullaniciId, String rol);
}
