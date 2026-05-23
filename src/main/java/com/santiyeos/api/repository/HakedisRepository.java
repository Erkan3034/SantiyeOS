package com.santiyeos.api.repository;

import com.santiyeos.api.model.Hakedis;
import com.santiyeos.api.model.PageResult;

public interface HakedisRepository {

    PageResult<Hakedis> listele(
            Integer firmaId,
            Integer kullaniciId,
            String rol,
            Integer taseronId,
            String onayDurumu,
            int limit,
            int offset
    );

    Hakedis getir(Integer firmaId, Integer kullaniciId, String rol, Integer taseronId, Integer hakedisId);

    Integer ekle(Integer firmaId, Integer talepEdenId, String rol, Hakedis hakedis);

    Integer onayla(Integer firmaId, Integer hakedisId, Integer onaylayanId, String rol);

    Integer reddet(Integer firmaId, Integer hakedisId, Integer onaylayanId, String rol, String redGerekce);

    Integer sil(Integer firmaId, Integer hakedisId, Integer kullaniciId, String rol);
}