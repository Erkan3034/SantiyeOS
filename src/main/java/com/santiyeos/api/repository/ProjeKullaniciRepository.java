package com.santiyeos.api.repository;

import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.model.ProjeKullanici;

public interface ProjeKullaniciRepository {

    void ata(Integer firmaId, Integer projeId, Integer kullaniciId);

    void kaldir(Integer firmaId, Integer projeId, Integer kullaniciId);

    PageResult<ProjeKullanici> listele(Integer firmaId, Integer projeId, int limit, int offset);
}
