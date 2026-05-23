package com.santiyeos.api.repository;

import com.santiyeos.api.model.Odeme;
import com.santiyeos.api.model.PageResult;

public interface OdemeRepository {

    PageResult<Odeme> listele(
            Integer firmaId,
            Integer kullaniciId,
            String rol,
            Integer taseronId,
            Integer hakedisId,
            int limit,
            int offset
    );

    Odeme getir(Integer firmaId, Integer kullaniciId, String rol, Integer taseronId, Integer odemeId);

    Integer ekle(Integer firmaId, Integer kaydedenId, String rol, Odeme odeme);

    Integer sil(Integer firmaId, Integer odemeId, Integer kullaniciId, String rol);
}
