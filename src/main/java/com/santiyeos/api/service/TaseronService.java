package com.santiyeos.api.service;

import com.santiyeos.api.dto.request.CreateTaseronRequest;
import com.santiyeos.api.dto.response.TaseronResponse;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.model.Taseron;

public interface TaseronService {
    PageResult<TaseronResponse> listele(Integer firmaId, int limit, int offset);
    TaseronResponse getir(Integer firmaId, Integer taseronId);
    TaseronResponse ekle(Integer firmaId, CreateTaseronRequest request);
}
