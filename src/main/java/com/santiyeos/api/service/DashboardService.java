package com.santiyeos.api.service;

import com.santiyeos.api.dto.response.DashboardOzetResponse;

public interface DashboardService {

    DashboardOzetResponse ozet(Integer firmaId, String rol);
}
