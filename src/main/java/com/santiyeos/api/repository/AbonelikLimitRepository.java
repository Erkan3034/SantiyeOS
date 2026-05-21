package com.santiyeos.api.repository;

public interface AbonelikLimitRepository {

    boolean aktifAbonelikVarMi(Integer firmaId);

    int planLimit(Integer firmaId, String kaynakTipi);

    int kullanimSayisi(Integer firmaId, String kaynakTipi);
}