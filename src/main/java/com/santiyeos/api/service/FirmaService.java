package com.santiyeos.api.service;

import com.santiyeos.api.dto.request.CreateFirmaRequest;
import com.santiyeos.api.dto.request.UpdateFirmaRequest;
import com.santiyeos.api.dto.response.FirmaResponse;
import com.santiyeos.api.model.PageResult;

public interface FirmaService {

    PageResult<FirmaResponse> listele(Boolean aktif, int limit, int offset);

    FirmaResponse getir(Integer firmaId);

    FirmaResponse ekle(CreateFirmaRequest request);

    FirmaResponse guncelle(Integer firmaId, UpdateFirmaRequest request);

    void pasiflestir(Integer firmaId);
}

//service iş kurralairyla konusur