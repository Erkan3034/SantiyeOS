package com.santiyeos.api.repository;

import com.santiyeos.api.model.Abonelik;
import com.santiyeos.api.model.PageResult;

import java.time.LocalDate;

public interface AbonelikRepository {

    Abonelik aktifGetir(Integer firmaId);

    PageResult<Abonelik> listele(Integer firmaId, int limit, int offset);

    Integer baslat(Integer firmaId, Integer planId, LocalDate baslangicTarihi, LocalDate bitisTarihi, Boolean deneme);

    Integer iptal(Integer abonelikId, Integer firmaId);
}