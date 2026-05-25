package com.santiyeos.api.controller;

import com.santiyeos.api.dto.response.LookupResponse;
import com.santiyeos.api.security.CurrentUser;
import com.santiyeos.api.security.CurrentUserContext;
import com.santiyeos.api.service.LookupService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lookups")
public class LookupController {

    private final LookupService lookupService;
    private final CurrentUserContext currentUserContext;

    public LookupController(LookupService lookupService, CurrentUserContext currentUserContext) {
        this.lookupService = lookupService;
        this.currentUserContext = currentUserContext;
    }

    @GetMapping("/projeler")
    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN, T(com.santiyeos.api.security.Roles).PROJE_YONETICISI)")
    public List<LookupResponse.Proje> projeler(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);
        return lookupService.projeler(firmaId, kullaniciId, currentUser.getRol());
    }

    @GetMapping("/taseronlar")
    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN, T(com.santiyeos.api.security.Roles).PROJE_YONETICISI, T(com.santiyeos.api.security.Roles).SAHA_PERSONELI)")
    public List<LookupResponse.Taseron> taseronlar(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        return lookupService.taseronlar(firmaId);
    }

    @GetMapping("/kullanicilar")
    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN, T(com.santiyeos.api.security.Roles).PROJE_YONETICISI)")
    public List<LookupResponse.Kullanici> kullanicilar(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @RequestParam(required = false) String rol
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        return lookupService.kullanicilar(firmaId, rol);
    }

    @GetMapping("/malzemeler")
    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN, T(com.santiyeos.api.security.Roles).PROJE_YONETICISI, T(com.santiyeos.api.security.Roles).SAHA_PERSONELI)")
    public List<LookupResponse.Malzeme> malzemeler(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        return lookupService.malzemeler(firmaId);
    }

    @GetMapping("/abonelik-planlari")
    @PreAuthorize("isAuthenticated()")
    public List<LookupResponse.AbonelikPlan> abonelikPlanlari() {
        return lookupService.abonelikPlanlari();
    }
}
