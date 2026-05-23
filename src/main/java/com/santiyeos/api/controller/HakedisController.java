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
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN, T(com.santiyeos.api.security.Roles).PROJE_YONETICISI, T(com.santiyeos.api.security.Roles).SAHA_PERSONELI, T(com.santiyeos.api.security.Roles).TASERON_TEMSILCI)")
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
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);
        Integer scopedTaseronId = currentUserContext.resolveTaseronScope(currentUser, taseronId);

        return hakedisService.listele(
                firmaId,
                kullaniciId,
                currentUser.getRol(),
                scopedTaseronId,
                onayDurumu,
                limit,
                offset
        );
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN, T(com.santiyeos.api.security.Roles).PROJE_YONETICISI, T(com.santiyeos.api.security.Roles).SAHA_PERSONELI, T(com.santiyeos.api.security.Roles).TASERON_TEMSILCI)")
    @GetMapping("/{hakedisId}")
    public HakedisResponse getir(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer hakedisId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);
        Integer scopedTaseronId = currentUserContext.resolveTaseronScope(currentUser, null);

        return hakedisService.getir(
                firmaId,
                kullaniciId,
                currentUser.getRol(),
                scopedTaseronId,
                hakedisId
        );
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN, T(com.santiyeos.api.security.Roles).PROJE_YONETICISI, T(com.santiyeos.api.security.Roles).TASERON_TEMSILCI)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HakedisResponse ekle(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @Valid @RequestBody CreateHakedisRequest request
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        currentUserContext.resolveTaseronScope(currentUser, null);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);

        return hakedisService.ekle(
                firmaId,
                kullaniciId,
                currentUser.getRol(),
                request
        );
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN, T(com.santiyeos.api.security.Roles).PROJE_YONETICISI)")
    @PatchMapping("/{hakedisId}/onayla")
    public HakedisResponse onayla(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer hakedisId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);

        return hakedisService.onayla(
                firmaId,
                hakedisId,
                kullaniciId,
                currentUser.getRol()
        );
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN, T(com.santiyeos.api.security.Roles).PROJE_YONETICISI)")
    @PatchMapping("/{hakedisId}/reddet")
    public HakedisResponse reddet(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer hakedisId,
            @Valid @RequestBody RejectHakedisRequest request
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);

        return hakedisService.reddet(
                firmaId,
                hakedisId,
                kullaniciId,
                currentUser.getRol(),
                request
        );
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN)")
    @DeleteMapping("/{hakedisId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sil(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer hakedisId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);

        hakedisService.sil(
                firmaId,
                hakedisId,
                kullaniciId,
                currentUser.getRol()
        );
    }
}