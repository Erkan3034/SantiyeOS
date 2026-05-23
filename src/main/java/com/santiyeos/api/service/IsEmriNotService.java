package com.santiyeos.api.service;

import com.santiyeos.api.dto.request.CreateIsEmriNotRequest;
import com.santiyeos.api.dto.request.UpdateIsEmriNotRequest;
import com.santiyeos.api.dto.response.IsEmriNotResponse;
import com.santiyeos.api.model.PageResult;

public interface IsEmriNotService {

    PageResult<IsEmriNotResponse> listele(Integer firmaId, Integer kullaniciId, Integer isEmriId, int limit, int offset);

    IsEmriNotResponse ekle(Integer firmaId, Integer kullaniciId, Integer isEmriId, CreateIsEmriNotRequest request);

    IsEmriNotResponse guncelle(Integer firmaId, Integer kullaniciId, Integer isEmriId, Integer notId, UpdateIsEmriNotRequest request);

    void sil(Integer firmaId, Integer kullaniciId, Integer isEmriId, Integer notId);
}
