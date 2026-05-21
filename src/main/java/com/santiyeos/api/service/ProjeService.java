package com.santiyeos.api.service;


import com.santiyeos.api.dto.request.CreateProjeRequest;
import com.santiyeos.api.dto.request.UpdateProjeRequest;
import com.santiyeos.api.dto.response.ProjeResponse;
import com.santiyeos.api.model.PageResult;

public interface ProjeService {

    PageResult<ProjeResponse> listele(Integer firmaId, Integer kullaniciId, String rol, String durum, int limit, int offset);

    ProjeResponse getir(Integer firmaId, Integer kullaniciId, String rol, Integer projeId);

    ProjeResponse ekle(Integer firmaId, CreateProjeRequest request);

    ProjeResponse guncelle(Integer firmaId, Integer projeId, UpdateProjeRequest request);

    void sil(Integer firmaId, Integer projeId, Integer kullaniciId);
}
