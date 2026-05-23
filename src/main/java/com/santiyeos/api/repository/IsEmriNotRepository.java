package com.santiyeos.api.repository;

import com.santiyeos.api.model.IsEmriNot;
import com.santiyeos.api.model.PageResult;

public interface IsEmriNotRepository {

    PageResult<IsEmriNot> listele(Integer firmaId, Integer kullaniciId, Integer isEmriId, int limit, int offset);

    IsEmriNot getir(Integer firmaId, Integer kullaniciId, Integer notId);

    Integer ekle(Integer firmaId, Integer kullaniciId, Integer isEmriId, IsEmriNot isEmriNot);

    Integer guncelle(Integer firmaId, Integer kullaniciId, Integer notId, IsEmriNot isEmriNot);

    Integer sil(Integer firmaId, Integer kullaniciId, Integer notId);
}
