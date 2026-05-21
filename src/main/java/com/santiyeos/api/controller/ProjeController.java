package com.santiyeos.api.controller;

import com.santiyeos.api.dto.request.CreateProjeRequest;
import com.santiyeos.api.dto.request.UpdateProjeRequest;
import com.santiyeos.api.dto.response.ProjeResponse;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.security.CurrentUser;
import com.santiyeos.api.security.CurrentUserContext;
import com.santiyeos.api.service.ProjeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController
@RequestMapping("/api/projeler")
public class ProjeController {

    private final ProjeService projeService;
    private final CurrentUserContext currentUserContext;

    public ProjeController(ProjeService projeService, CurrentUserContext currentUserContext) {
        this.projeService = projeService;
        this.currentUserContext = currentUserContext;
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN, T(com.santiyeos.api.security.Roles).PROJE_YONETICISI)")
    @GetMapping
    public PageResult<ProjeResponse> listele(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @RequestParam(required = false) String durum,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);
        return projeService.listele(firmaId, kullaniciId, currentUser.getRol(), durum, limit, offset);
    }


    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN, T(com.santiyeos.api.security.Roles).PROJE_YONETICISI)")
    @GetMapping("/{projeId}")
    public ProjeResponse getir(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer projeId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);
        return projeService.getir(firmaId, kullaniciId, currentUser.getRol(), projeId);
    }


    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjeResponse ekle(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @Valid @RequestBody CreateProjeRequest request
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        return projeService.ekle(firmaId, request);
    }


    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN)")
    @PutMapping("/{projeId}")
    public ProjeResponse guncelle(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer projeId,
            @Valid @RequestBody UpdateProjeRequest request
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        return projeService.guncelle(firmaId, projeId, request);
    }


    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN)")
    @DeleteMapping("/{projeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sil(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer projeId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);
        projeService.sil(firmaId, projeId, kullaniciId);
    }
}
