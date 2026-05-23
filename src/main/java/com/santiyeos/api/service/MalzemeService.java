package com.santiyeos.api.service;

import com.santiyeos.api.dto.request.CreateMalzemeRequest;
import com.santiyeos.api.dto.request.UpdateMalzemeRequest;
import com.santiyeos.api.dto.response.MalzemeResponse;
import com.santiyeos.api.model.PageResult;

public interface MalzemeService {

    PageResult<MalzemeResponse> listele(
            Integer firmaId,
            Integer kategoriId,
            Boolean kritikStok,
            int limit,
            int offset
    );

    MalzemeResponse getir(Integer firmaId, Integer malzemeId);

    MalzemeResponse ekle(Integer firmaId, String rol, CreateMalzemeRequest request);

    MalzemeResponse guncelle(Integer firmaId, Integer malzemeId, String rol, UpdateMalzemeRequest request);

    void sil(Integer firmaId, Integer malzemeId, Integer kullaniciId, String rol);
}
