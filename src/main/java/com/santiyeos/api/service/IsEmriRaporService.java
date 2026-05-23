package com.santiyeos.api.service;

import com.santiyeos.api.dto.request.CreateIsEmriRaporRequest;
import com.santiyeos.api.dto.request.UpdateIsEmriRaporRequest;
import com.santiyeos.api.dto.response.IsEmriRaporResponse;
import com.santiyeos.api.model.PageResult;

public interface IsEmriRaporService {

    PageResult<IsEmriRaporResponse> listele(Integer firmaId, Integer kullaniciId, Integer isEmriId, int limit, int offset);

    IsEmriRaporResponse ekle(Integer firmaId, Integer kullaniciId, Integer isEmriId, CreateIsEmriRaporRequest request);

    IsEmriRaporResponse guncelle(Integer firmaId, Integer kullaniciId, Integer isEmriId, Integer raporId, UpdateIsEmriRaporRequest request);

    void sil(Integer firmaId, Integer kullaniciId, Integer isEmriId, Integer raporId);
}
