package com.santiyeos.api.controller;

import com.santiyeos.api.dto.request.CreateOdemeRequest;
import com.santiyeos.api.dto.response.OdemeResponse;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.security.CurrentUser;
import com.santiyeos.api.security.CurrentUserContext;
import com.santiyeos.api.service.OdemeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/odemeler")
public class OdemeController {

    private final OdemeService odemeService;
    private final CurrentUserContext currentUserContext;

    public OdemeController(OdemeService odemeService, CurrentUserContext currentUserContext) {
        this.odemeService = odemeService;
        this.currentUserContext = currentUserContext;
    }

    @GetMapping
    public PageResult<OdemeResponse> listele(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @RequestParam(required = false) Integer hakedisId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        return odemeService.listele(firmaId, hakedisId, limit, offset);
    }

    @GetMapping("/{odemeId}")
    public OdemeResponse getir(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer odemeId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        return odemeService.getir(firmaId, odemeId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OdemeResponse ekle(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @Valid @RequestBody CreateOdemeRequest request
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);
        return odemeService.ekle(firmaId, kullaniciId, request);
    }

    @DeleteMapping("/{odemeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sil(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer odemeId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);
        odemeService.sil(firmaId, odemeId, kullaniciId);
    }
}
