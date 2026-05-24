package com.santiyeos.api.service;

import com.santiyeos.api.dto.response.GenelOzetRaporuResponse;
import com.santiyeos.api.dto.response.ProjeMaliyetRaporuResponse;
import com.santiyeos.api.dto.response.TaseronPerformansRaporuResponse;

import java.util.List;

public interface RaporService {

    GenelOzetRaporuResponse genelOzet(Integer firmaId);

    ProjeMaliyetRaporuResponse projeMaliyet(Integer firmaId, Integer kullaniciId, String rol, Integer projeId);

    List<TaseronPerformansRaporuResponse> taseronPerformans(Integer firmaId, Integer kullaniciId, String rol);
}
