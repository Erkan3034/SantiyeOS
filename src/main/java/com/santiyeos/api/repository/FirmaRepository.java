package com.santiyeos.api.repository;
import com.santiyeos.api.model.Firma;
import com.santiyeos.api.model.PageResult;

public interface FirmaRepository {
    PageResult<Firma> listele(Boolean aktif, int limit, int offset);

    Firma getir(Integer firmaId);

    Integer ekle(Firma firma);

    Integer guncelle(Integer firmaId, Firma firma);

    Integer pasiflestir(Integer firmaId);
}


// service katmanının DB ye hangi operasyonlarla erişeceğini tarif eder.