package com.santiyeos.api.controller;

import com.santiyeos.api.dto.response.DashboardOzetResponse;
import com.santiyeos.api.security.CurrentUser;
import com.santiyeos.api.security.CurrentUserContext;
import com.santiyeos.api.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final CurrentUserContext currentUserContext;

    public DashboardController(DashboardService dashboardService, CurrentUserContext currentUserContext) {
        this.dashboardService = dashboardService;
        this.currentUserContext = currentUserContext;
    }

    @GetMapping("/ozet")
    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN)")
    public DashboardOzetResponse ozet(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        return dashboardService.ozet(firmaId, currentUser.getRol());
    }
}
