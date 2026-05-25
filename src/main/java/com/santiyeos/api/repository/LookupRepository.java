package com.santiyeos.api.repository;

import com.santiyeos.api.model.LookupItem;

import java.util.List;

public interface LookupRepository {

    List<LookupItem.Proje> projeler(Integer firmaId, Integer kullaniciId, String rol);

    List<LookupItem.Taseron> taseronlar(Integer firmaId);

    List<LookupItem.Kullanici> kullanicilar(Integer firmaId, String rol);

    List<LookupItem.Malzeme> malzemeler(Integer firmaId);

    List<LookupItem.AbonelikPlan> abonelikPlanlari();
}
