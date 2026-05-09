package com.santiyeos.api.service;

import com.santiyeos.api.dto.request.CreateHakedisRequest;
import com.santiyeos.api.dto.request.RejectHakedisRequest;
import com.santiyeos.api.dto.response.HakedisResponse;
import com.santiyeos.api.model.PageResult;

public interface HakedisService {

    PageResult<HakedisResponse> listele(Integer firmaId, Integer taseronId, String onayDurumu, int limit, int offset);

    HakedisResponse getir(Integer firmaId, Integer hakedisId);

    HakedisResponse ekle(Integer firmaId, Integer kullaniciId, CreateHakedisRequest request);

    HakedisResponse onayla(Integer firmaId, Integer hakedisId, Integer kullaniciId);

    HakedisResponse reddet(Integer firmaId, Integer hakedisId, Integer kullaniciId, RejectHakedisRequest request);

    void sil(Integer firmaId, Integer hakedisId, Integer kullaniciId);
}
