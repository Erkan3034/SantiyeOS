package com.santiyeos.api.service;

import com.santiyeos.api.dto.request.CreateStokHareketRequest;
import com.santiyeos.api.dto.response.StokHareketResponse;
import com.santiyeos.api.model.PageResult;

public interface StokHareketService {

    PageResult<StokHareketResponse> listele(
            Integer firmaId,
            Integer malzemeId,
            Integer projeId,
            Integer isEmriId,
            String hareketTipi,
            int limit,
            int offset
    );

    StokHareketResponse getir(Integer firmaId, Integer hareketId);

    StokHareketResponse ekle(Integer firmaId, Integer kaydedenId, String rol, CreateStokHareketRequest request);
}
