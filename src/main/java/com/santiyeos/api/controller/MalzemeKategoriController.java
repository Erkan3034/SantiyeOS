package com.santiyeos.api.controller;

import com.santiyeos.api.dto.request.CreateMalzemeKategoriRequest;
import com.santiyeos.api.dto.request.UpdateMalzemeKategoriRequest;
import com.santiyeos.api.dto.response.MalzemeKategoriResponse;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.security.CurrentUser;
import com.santiyeos.api.security.CurrentUserContext;
import com.santiyeos.api.service.MalzemeKategoriService;
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
@RequestMapping("/api/malzeme-kategorileri")
public class MalzemeKategoriController {

    private final MalzemeKategoriService malzemeKategoriService;
    private final CurrentUserContext currentUserContext;

    public MalzemeKategoriController(
            MalzemeKategoriService malzemeKategoriService,
            CurrentUserContext currentUserContext
    ) {
        this.malzemeKategoriService = malzemeKategoriService;
        this.currentUserContext = currentUserContext;
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN, T(com.santiyeos.api.security.Roles).PROJE_YONETICISI, T(com.santiyeos.api.security.Roles).SAHA_PERSONELI)")
    @GetMapping
    public PageResult<MalzemeKategoriResponse> listele(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        return malzemeKategoriService.listele(firmaId, limit, offset);
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN, T(com.santiyeos.api.security.Roles).PROJE_YONETICISI, T(com.santiyeos.api.security.Roles).SAHA_PERSONELI)")
    @GetMapping("/{kategoriId}")
    public MalzemeKategoriResponse getir(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer kategoriId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        return malzemeKategoriService.getir(firmaId, kategoriId);
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN, T(com.santiyeos.api.security.Roles).PROJE_YONETICISI)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MalzemeKategoriResponse ekle(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @Valid @RequestBody CreateMalzemeKategoriRequest request
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        return malzemeKategoriService.ekle(firmaId, currentUser.getRol(), request);
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN, T(com.santiyeos.api.security.Roles).PROJE_YONETICISI)")
    @PutMapping("/{kategoriId}")
    public MalzemeKategoriResponse guncelle(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer kategoriId,
            @Valid @RequestBody UpdateMalzemeKategoriRequest request
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        return malzemeKategoriService.guncelle(firmaId, kategoriId, currentUser.getRol(), request);
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN, T(com.santiyeos.api.security.Roles).PROJE_YONETICISI)")
    @DeleteMapping("/{kategoriId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sil(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer kategoriId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);
        malzemeKategoriService.sil(firmaId, kategoriId, kullaniciId, currentUser.getRol());
    }
}
