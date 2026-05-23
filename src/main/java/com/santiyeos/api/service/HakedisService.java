package com.santiyeos.api.service;

import com.santiyeos.api.dto.request.CreateHakedisRequest;
import com.santiyeos.api.dto.request.RejectHakedisRequest;
import com.santiyeos.api.dto.response.HakedisResponse;
import com.santiyeos.api.model.PageResult;

public interface HakedisService {

    PageResult<HakedisResponse> listele(
            Integer firmaId,
            Integer kullaniciId,
            String rol,
            Integer taseronId,
            String onayDurumu,
            int limit,
            int offset
    );

    HakedisResponse getir(Integer firmaId, Integer kullaniciId, String rol, Integer taseronId, Integer hakedisId);

    HakedisResponse ekle(Integer firmaId, Integer kullaniciId, String rol, CreateHakedisRequest request);

    HakedisResponse onayla(Integer firmaId, Integer hakedisId, Integer kullaniciId, String rol);

    HakedisResponse reddet(Integer firmaId, Integer hakedisId, Integer kullaniciId, String rol, RejectHakedisRequest request);

    void sil(Integer firmaId, Integer hakedisId, Integer kullaniciId, String rol);
}