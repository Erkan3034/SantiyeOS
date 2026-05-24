package com.santiyeos.api.controller;

import com.santiyeos.api.dto.response.GenelOzetRaporuResponse;
import com.santiyeos.api.dto.response.ProjeMaliyetRaporuResponse;
import com.santiyeos.api.dto.response.TaseronPerformansRaporuResponse;
import com.santiyeos.api.security.CurrentUser;
import com.santiyeos.api.security.CurrentUserContext;
import com.santiyeos.api.security.Roles;
import com.santiyeos.api.service.RaporService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/raporlar")
public class RaporController {

    private final RaporService raporService;
    private final CurrentUserContext currentUserContext;

    public RaporController(RaporService raporService, CurrentUserContext currentUserContext) {
        this.raporService = raporService;
        this.currentUserContext = currentUserContext;
    }

    @GetMapping("/genel-ozet")
    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN)")
    public GenelOzetRaporuResponse genelOzet(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        return raporService.genelOzet(firmaId);
    }

    @GetMapping("/projeler/{projeId}/maliyet")
    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN, T(com.santiyeos.api.security.Roles).PROJE_YONETICISI)")
    public ProjeMaliyetRaporuResponse projeMaliyet(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer projeId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);
        return raporService.projeMaliyet(firmaId, kullaniciId, currentUser.getRol(), projeId);
    }

    @GetMapping("/taseron-performans")
    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN, T(com.santiyeos.api.security.Roles).PROJE_YONETICISI)")
    public List<TaseronPerformansRaporuResponse> taseronPerformans(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);
        return raporService.taseronPerformans(firmaId, kullaniciId, currentUser.getRol());
    }
}
