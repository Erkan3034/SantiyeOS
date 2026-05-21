package com.santiyeos.api.service;

import com.santiyeos.api.dto.request.BaslatAbonelikRequest;
import com.santiyeos.api.dto.response.AbonelikResponse;
import com.santiyeos.api.model.PageResult;

public interface AbonelikService {

    AbonelikResponse aktifGetir(Integer firmaId);

    PageResult<AbonelikResponse> listele(Integer firmaId, int limit, int offset);

    AbonelikResponse baslat(Integer firmaId, BaslatAbonelikRequest request);

    void iptal(Integer abonelikId, Integer firmaId);
}