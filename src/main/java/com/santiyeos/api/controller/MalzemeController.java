package com.santiyeos.api.controller;

import com.santiyeos.api.dto.request.CreateMalzemeRequest;
import com.santiyeos.api.dto.request.UpdateMalzemeRequest;
import com.santiyeos.api.dto.response.MalzemeResponse;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.security.CurrentUser;
import com.santiyeos.api.security.CurrentUserContext;
import com.santiyeos.api.service.MalzemeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/malzemeler")
public class MalzemeController {

    private final MalzemeService malzemeService;
    private final CurrentUserContext currentUserContext;

    public MalzemeController(MalzemeService malzemeService, CurrentUserContext currentUserContext) {
        this.malzemeService = malzemeService;
        this.currentUserContext = currentUserContext;
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN, T(com.santiyeos.api.security.Roles).PROJE_YONETICISI, T(com.santiyeos.api.security.Roles).SAHA_PERSONELI)")
    @GetMapping
    public PageResult<MalzemeResponse> listele(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @RequestParam(required = false) Integer kategoriId,
            @RequestParam(required = false) Boolean kritikStok,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        return malzemeService.listele(firmaId, kategoriId, kritikStok, limit, offset);
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN, T(com.santiyeos.api.security.Roles).PROJE_YONETICISI, T(com.santiyeos.api.security.Roles).SAHA_PERSONELI)")
    @GetMapping("/{malzemeId}")
    public MalzemeResponse getir(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer malzemeId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        return malzemeService.getir(firmaId, malzemeId);
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN, T(com.santiyeos.api.security.Roles).PROJE_YONETICISI)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MalzemeResponse ekle(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @Valid @RequestBody CreateMalzemeRequest request
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        return malzemeService.ekle(firmaId, currentUser.getRol(), request);
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN, T(com.santiyeos.api.security.Roles).PROJE_YONETICISI)")
    @PutMapping("/{malzemeId}")
    public MalzemeResponse guncelle(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer malzemeId,
            @Valid @RequestBody UpdateMalzemeRequest request
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        return malzemeService.guncelle(firmaId, malzemeId, currentUser.getRol(), request);
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN, T(com.santiyeos.api.security.Roles).PROJE_YONETICISI)")
    @DeleteMapping("/{malzemeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sil(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer malzemeId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);
        malzemeService.sil(firmaId, malzemeId, kullaniciId, currentUser.getRol());
    }
}

// malzeme endpointeri