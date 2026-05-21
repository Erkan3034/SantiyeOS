package com.santiyeos.api.controller;

import com.santiyeos.api.dto.response.AbonelikPlanResponse;
import com.santiyeos.api.service.AbonelikPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/abonelik-planlari")
@RequiredArgsConstructor
public class AbonelikPlanController {

    private final AbonelikPlanService abonelikPlanService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AbonelikPlanResponse>> listele(
            @RequestParam(required = false) Boolean aktif
    ) {
        return ResponseEntity.ok(abonelikPlanService.listele(aktif));
    }

    @GetMapping("/{planId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AbonelikPlanResponse> getir(@PathVariable Integer planId) {
        return ResponseEntity.ok(abonelikPlanService.getir(planId));
    }
}