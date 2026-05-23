package com.santiyeos.api.service;

import com.santiyeos.api.dto.request.CreateMalzemeKategoriRequest;
import com.santiyeos.api.dto.request.UpdateMalzemeKategoriRequest;
import com.santiyeos.api.dto.response.MalzemeKategoriResponse;
import com.santiyeos.api.model.PageResult;

public interface MalzemeKategoriService {

    PageResult<MalzemeKategoriResponse> listele(Integer firmaId, int limit, int offset);

    MalzemeKategoriResponse getir(Integer firmaId, Integer kategoriId);

    MalzemeKategoriResponse ekle(Integer firmaId, String rol, CreateMalzemeKategoriRequest request);

    MalzemeKategoriResponse guncelle(
            Integer firmaId,
            Integer kategoriId,
            String rol,
            UpdateMalzemeKategoriRequest request
    );

    void sil(Integer firmaId, Integer kategoriId, Integer kullaniciId, String rol);
}
