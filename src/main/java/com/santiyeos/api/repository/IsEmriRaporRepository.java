package com.santiyeos.api.repository;

import com.santiyeos.api.model.IsEmriRapor;
import com.santiyeos.api.model.PageResult;

public interface IsEmriRaporRepository {

    PageResult<IsEmriRapor> listele(Integer firmaId, Integer kullaniciId, Integer isEmriId, int limit, int offset);

    IsEmriRapor getir(Integer firmaId, Integer kullaniciId, Integer raporId);

    Integer ekle(Integer firmaId, Integer kullaniciId, Integer isEmriId, IsEmriRapor isEmriRapor);

    Integer guncelle(Integer firmaId, Integer kullaniciId, Integer raporId, IsEmriRapor isEmriRapor);

    Integer sil(Integer firmaId, Integer kullaniciId, Integer raporId);
}
