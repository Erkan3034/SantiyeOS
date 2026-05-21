package com.santiyeos.api.service;

import com.santiyeos.api.dto.response.AbonelikPlanResponse;

import java.util.List;

public interface AbonelikPlanService {

    List<AbonelikPlanResponse> listele(Boolean aktif);

    AbonelikPlanResponse getir(Integer planId);
}