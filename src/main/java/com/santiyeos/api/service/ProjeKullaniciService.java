package com.santiyeos.api.service;

import com.santiyeos.api.dto.response.ProjeKullaniciResponse;
import com.santiyeos.api.model.PageResult;

public interface ProjeKullaniciService {

    void ata(Integer firmaId, Integer projeId, Integer kullaniciId);

    void kaldir(Integer firmaId, Integer projeId, Integer kullaniciId);

    PageResult<ProjeKullaniciResponse> listele(Integer firmaId, Integer projeId, int limit, int offset);
}
