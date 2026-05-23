package com.santiyeos.api.repository;

import com.santiyeos.api.model.Bildirim;
import com.santiyeos.api.model.PageResult;

public interface BildirimRepository {

    PageResult<Bildirim> listele(Integer firmaId, Integer kullaniciId, Boolean sadeceOkunmamis, int limit, int offset);

    Bildirim getir(Integer firmaId, Integer kullaniciId, Integer bildirimId);

    Integer ekle(Integer firmaId, Integer kullaniciId, Integer olusturanId, Bildirim bildirim);

    Integer okunduIsaretle(Integer firmaId, Integer kullaniciId, Integer bildirimId);

    Integer tumunuOkunduIsaretle(Integer firmaId, Integer kullaniciId);

    Integer sil(Integer firmaId, Integer kullaniciId, Integer bildirimId);
}
