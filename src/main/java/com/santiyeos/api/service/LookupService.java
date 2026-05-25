package com.santiyeos.api.service;

import com.santiyeos.api.dto.response.LookupResponse;

import java.util.List;

public interface LookupService {

    List<LookupResponse.Proje> projeler(Integer firmaId, Integer kullaniciId, String rol);

    List<LookupResponse.Taseron> taseronlar(Integer firmaId);

    List<LookupResponse.Kullanici> kullanicilar(Integer firmaId, String rol);

    List<LookupResponse.Malzeme> malzemeler(Integer firmaId);

    List<LookupResponse.AbonelikPlan> abonelikPlanlari();
}
