package com.santiyeos.api.repository;

import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.model.StokHareket;

public interface StokHareketRepository {

    PageResult<StokHareket> listele(
            Integer firmaId,
            Integer malzemeId,
            Integer projeId,
            Integer isEmriId,
            String hareketTipi,
            int limit,
            int offset
    );

    StokHareket getir(Integer firmaId, Integer hareketId);

    Integer ekle(Integer firmaId, Integer kaydedenId, String rol, StokHareket hareket);
}
