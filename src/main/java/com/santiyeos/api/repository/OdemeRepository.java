package com.santiyeos.api.repository;

import com.santiyeos.api.model.Odeme;
import com.santiyeos.api.model.PageResult;

public interface OdemeRepository {

    PageResult<Odeme> listele(Integer firmaId, Integer hakedisId, int limit, int offset);

    Odeme getir(Integer firmaId, Integer odemeId);

    Integer ekle(Integer firmaId, Integer kaydedenId, Odeme odeme);

    Integer sil(Integer firmaId, Integer odemeId, Integer kullaniciId);
}
