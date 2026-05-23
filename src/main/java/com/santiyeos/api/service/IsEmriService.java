package com.santiyeos.api.service;

import com.santiyeos.api.dto.request.CreateIsEmriRequest;
import com.santiyeos.api.dto.request.UpdateIsEmriDurumRequest;
import com.santiyeos.api.dto.request.UpdateIsEmriRequest;
import com.santiyeos.api.dto.response.IsEmriResponse;
import com.santiyeos.api.model.PageResult;

public interface IsEmriService {

    PageResult<IsEmriResponse> listele(
            Integer firmaId,
            Integer kullaniciId,
            String rol,
            Integer projeId,
            Integer taseronId,
            String durum,
            int limit,
            int offset
    );

    IsEmriResponse getir(Integer firmaId, Integer kullaniciId, String rol, Integer taseronId, Integer isEmriId);

    IsEmriResponse ekle(Integer firmaId, Integer kullaniciId, String rol, CreateIsEmriRequest request);

    IsEmriResponse guncelle(Integer firmaId, Integer isEmriId, Integer kullaniciId, String rol, UpdateIsEmriRequest request);

    IsEmriResponse durumGuncelle(Integer firmaId, Integer isEmriId, Integer kullaniciId, String rol, UpdateIsEmriDurumRequest request);

    void sil(Integer firmaId, Integer isEmriId, Integer kullaniciId, String rol);
}
