package com.santiyeos.api.repository;

import com.santiyeos.api.model.GenelOzetRaporu;
import com.santiyeos.api.model.ProjeMaliyetRaporu;
import com.santiyeos.api.model.TaseronPerformansRaporu;

import java.util.List;

public interface RaporRepository {

    GenelOzetRaporu genelOzet(Integer firmaId);

    ProjeMaliyetRaporu projeMaliyet(Integer firmaId, Integer kullaniciId, String rol, Integer projeId);

    List<TaseronPerformansRaporu> taseronPerformans(Integer firmaId, Integer kullaniciId, String rol);
}
