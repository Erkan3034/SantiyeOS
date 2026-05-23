package com.santiyeos.api.service;

import com.santiyeos.api.dto.request.CreateBildirimRequest;
import com.santiyeos.api.dto.response.BildirimResponse;
import com.santiyeos.api.model.PageResult;

public interface BildirimService {

    PageResult<BildirimResponse> listele(Integer firmaId, Integer kullaniciId, Boolean sadeceOkunmamis, int limit, int offset);

    BildirimResponse getir(Integer firmaId, Integer kullaniciId, Integer bildirimId);

    BildirimResponse ekle(Integer firmaId, Integer olusturanId, CreateBildirimRequest request);

    BildirimResponse okunduIsaretle(Integer firmaId, Integer kullaniciId, Integer bildirimId);

    Integer tumunuOkunduIsaretle(Integer firmaId, Integer kullaniciId);

    void sil(Integer firmaId, Integer kullaniciId, Integer bildirimId);
}
