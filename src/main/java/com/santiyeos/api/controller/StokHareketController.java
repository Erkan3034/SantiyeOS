package com.santiyeos.api.controller;

import com.santiyeos.api.dto.request.CreateStokHareketRequest;
import com.santiyeos.api.dto.response.StokHareketResponse;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.security.CurrentUser;
import com.santiyeos.api.security.CurrentUserContext;
import com.santiyeos.api.service.StokHareketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stok-hareketleri")
public class StokHareketController {

    private final StokHareketService stokHareketService;
    private final CurrentUserContext currentUserContext;

    public StokHareketController(StokHareketService stokHareketService, CurrentUserContext currentUserContext) {
        this.stokHareketService = stokHareketService;
        this.currentUserContext = currentUserContext;
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN, T(com.santiyeos.api.security.Roles).PROJE_YONETICISI, T(com.santiyeos.api.security.Roles).SAHA_PERSONELI)")
    @GetMapping
    public PageResult<StokHareketResponse> listele(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @RequestParam(required = false) Integer malzemeId,
            @RequestParam(required = false) Integer projeId,
            @RequestParam(required = false) Integer isEmriId,
            @RequestParam(required = false) String hareketTipi,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        return stokHareketService.listele(firmaId, malzemeId, projeId, isEmriId, hareketTipi, limit, offset);
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN, T(com.santiyeos.api.security.Roles).PROJE_YONETICISI, T(com.santiyeos.api.security.Roles).SAHA_PERSONELI)")
    @GetMapping("/{hareketId}")
    public StokHareketResponse getir(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer hareketId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        return stokHareketService.getir(firmaId, hareketId);
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN, T(com.santiyeos.api.security.Roles).PROJE_YONETICISI, T(com.santiyeos.api.security.Roles).SAHA_PERSONELI)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StokHareketResponse ekle(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @Valid @RequestBody CreateStokHareketRequest request
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);
        return stokHareketService.ekle(firmaId, kullaniciId, currentUser.getRol(), request);
    }
}
