package com.santiyeos.api.controller;

import com.santiyeos.api.dto.request.CreateBildirimRequest;
import com.santiyeos.api.dto.response.BildirimResponse;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.security.CurrentUser;
import com.santiyeos.api.security.CurrentUserContext;
import com.santiyeos.api.service.BildirimService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/bildirimler")
public class BildirimController {

    private final BildirimService bildirimService;
    private final CurrentUserContext currentUserContext;

    public BildirimController(BildirimService bildirimService, CurrentUserContext currentUserContext) {
        this.bildirimService = bildirimService;
        this.currentUserContext = currentUserContext;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public PageResult<BildirimResponse> listele(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @RequestParam(defaultValue = "false") Boolean sadeceOkunmamis,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);
        return bildirimService.listele(firmaId, kullaniciId, sadeceOkunmamis, limit, offset);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{bildirimId}")
    public BildirimResponse getir(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer bildirimId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);
        return bildirimService.getir(firmaId, kullaniciId, bildirimId);
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BildirimResponse ekle(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @Valid @RequestBody CreateBildirimRequest request
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer olusturanId = currentUserContext.requireUserId(currentUser);
        return bildirimService.ekle(firmaId, olusturanId, request);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{bildirimId}/okundu")
    public BildirimResponse okunduIsaretle(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer bildirimId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);
        return bildirimService.okunduIsaretle(firmaId, kullaniciId, bildirimId);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/okundu")
    public Map<String, Integer> tumunuOkunduIsaretle(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);
        Integer etkilenenSatir = bildirimService.tumunuOkunduIsaretle(firmaId, kullaniciId);
        return Map.of("etkilenenSatir", etkilenenSatir);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{bildirimId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sil(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer bildirimId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);
        bildirimService.sil(firmaId, kullaniciId, bildirimId);
    }
}
