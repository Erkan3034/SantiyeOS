package com.santiyeos.api.controller;

import com.santiyeos.api.dto.request.CreateHakedisRequest;
import com.santiyeos.api.dto.request.RejectHakedisRequest;
import com.santiyeos.api.dto.response.HakedisResponse;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.security.CurrentUser;
import com.santiyeos.api.security.CurrentUserContext;
import com.santiyeos.api.service.HakedisService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hakedisler")
public class HakedisController {

    private final HakedisService hakedisService;
    private final CurrentUserContext currentUserContext;

    public HakedisController(HakedisService hakedisService, CurrentUserContext currentUserContext) {
        this.hakedisService = hakedisService;
        this.currentUserContext = currentUserContext;
    }

    @GetMapping
    public PageResult<HakedisResponse> listele(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @RequestParam(required = false) Integer taseronId,
            @RequestParam(required = false) String onayDurumu,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        return hakedisService.listele(firmaId, taseronId, onayDurumu, limit, offset);
    }

    @GetMapping("/{hakedisId}")
    public HakedisResponse getir(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer hakedisId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        return hakedisService.getir(firmaId, hakedisId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HakedisResponse ekle(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @Valid @RequestBody CreateHakedisRequest request
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);
        return hakedisService.ekle(firmaId, kullaniciId, request);
    }

    @PatchMapping("/{hakedisId}/onayla")
    public HakedisResponse onayla(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer hakedisId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);
        return hakedisService.onayla(firmaId, hakedisId, kullaniciId);
    }

    @PatchMapping("/{hakedisId}/reddet")
    public HakedisResponse reddet(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer hakedisId,
            @Valid @RequestBody RejectHakedisRequest request
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);
        return hakedisService.reddet(firmaId, hakedisId, kullaniciId, request);
    }

    @DeleteMapping("/{hakedisId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sil(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer hakedisId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);
        hakedisService.sil(firmaId, hakedisId, kullaniciId);
    }
}
