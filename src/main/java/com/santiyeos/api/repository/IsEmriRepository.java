package com.santiyeos.api.repository;

import com.santiyeos.api.model.IsEmri;
import com.santiyeos.api.model.PageResult;

public interface IsEmriRepository {

    PageResult<IsEmri> listele(Integer firmaId, Integer projeId, Integer taseronId, String durum, int limit, int offset);

    IsEmri getir(Integer firmaId, Integer taseronId, Integer isEmriId);

    Integer ekle(Integer firmaId, Integer olusturanId, IsEmri isEmri);

    Integer guncelle(Integer firmaId, Integer isEmriId, Integer kullaniciId, IsEmri isEmri);

    Integer durumGuncelle(Integer firmaId, Integer isEmriId, Integer kullaniciId, String yeniDurum, String aciklama);

    Integer sil(Integer firmaId, Integer isEmriId, Integer kullaniciId);
}
