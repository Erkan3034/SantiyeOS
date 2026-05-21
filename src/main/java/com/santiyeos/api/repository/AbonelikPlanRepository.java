package com.santiyeos.api.repository;

import com.santiyeos.api.model.AbonelikPlan;

import java.util.List;

public interface AbonelikPlanRepository {

    List<AbonelikPlan> listele(Boolean aktif);

    AbonelikPlan getir(Integer planId);
}