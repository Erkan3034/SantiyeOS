package com.santiyeos.api.service;

import com.santiyeos.api.dto.request.CreateOdemeRequest;
import com.santiyeos.api.dto.response.OdemeResponse;
import com.santiyeos.api.model.PageResult;

public interface OdemeService {

    PageResult<OdemeResponse> listele(Integer firmaId, Integer hakedisId, int limit, int offset);

    OdemeResponse getir(Integer firmaId, Integer odemeId);

    OdemeResponse ekle(Integer firmaId, Integer kullaniciId, CreateOdemeRequest request);

    void sil(Integer firmaId, Integer odemeId, Integer kullaniciId);
}
