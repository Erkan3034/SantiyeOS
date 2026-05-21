package com.santiyeos.api.service.impl;

import com.santiyeos.api.dto.response.AbonelikPlanResponse;
import com.santiyeos.api.exception.BusinessException;
import com.santiyeos.api.model.AbonelikPlan;
import com.santiyeos.api.repository.AbonelikPlanRepository;
import com.santiyeos.api.service.AbonelikPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AbonelikPlanServiceImpl implements AbonelikPlanService {

    private final AbonelikPlanRepository abonelikPlanRepository;

    @Override
    public List<AbonelikPlanResponse> listele(Boolean aktif) {
        return abonelikPlanRepository.listele(aktif)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AbonelikPlanResponse getir(Integer planId) {
        Integer safePlanId = validatePositive(planId, "Geçerli bir abonelik plan id giriniz.");

        AbonelikPlan plan = abonelikPlanRepository.getir(safePlanId);
        if (plan == null) {
            throw BusinessException.notFound("Abonelik planı bulunamadı.");
        }

        return toResponse(plan);
    }

    private AbonelikPlanResponse toResponse(AbonelikPlan plan) {
        return AbonelikPlanResponse.builder()
                .planId(plan.getPlanId())
                .ad(plan.getAd())
                .maxProje(plan.getMaxProje())
                .maxKullanici(plan.getMaxKullanici())
                .maxTaseron(plan.getMaxTaseron())
                .aylikUcret(plan.getAylikUcret())
                .aktif(plan.getAktif())
                .build();
    }

    private Integer validatePositive(Integer value, String message) {
        if (value == null || value <= 0) {
            throw BusinessException.badRequest(message);
        }

        return value;
    }
}