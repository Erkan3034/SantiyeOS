package com.santiyeos.api.service;

import com.santiyeos.api.dto.request.CreateKullaniciRequest;
import com.santiyeos.api.dto.request.ResetKullaniciSifreRequest;
import com.santiyeos.api.dto.request.UpdateKullaniciRequest;
import com.santiyeos.api.dto.response.KullaniciResponse;
import com.santiyeos.api.model.Kullanici;
import com.santiyeos.api.model.PageResult;


public interface KullaniciService {

    PageResult<KullaniciResponse> listele(Integer firmaId, String rol, Boolean aktif, int limit, int offset);

    KullaniciResponse getir(Integer firmaId, Integer kullaniciId);

    KullaniciResponse ekle(Integer firmaId, CreateKullaniciRequest request);

    KullaniciResponse guncelle(Integer firmaId, Integer kullaniciId, UpdateKullaniciRequest request);

    void sil(Integer firmaId, Integer kullaniciId);

    void sifreResetle(Integer firmaId, Integer kullaniciId, ResetKullaniciSifreRequest request);

}
