package com.santiyeos.api.controller;

import com.santiyeos.api.dto.request.CreateIsEmriRequest;
import com.santiyeos.api.dto.request.UpdateIsEmriDurumRequest;
import com.santiyeos.api.dto.request.UpdateIsEmriRequest;
import com.santiyeos.api.dto.response.IsEmriResponse;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.security.CurrentUser;
import com.santiyeos.api.security.CurrentUserContext;
import com.santiyeos.api.service.IsEmriService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/is-emirleri")
public class IsEmriController {

    private final IsEmriService isEmriService;
    private final CurrentUserContext currentUserContext;

    public IsEmriController(IsEmriService isEmriService, CurrentUserContext currentUserContext) {
        this.isEmriService = isEmriService;
        this.currentUserContext = currentUserContext;
    }

    @GetMapping
    public PageResult<IsEmriResponse> listele(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @RequestParam(required = false) Integer projeId,
            @RequestParam(required = false) String durum,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        return isEmriService.listele(firmaId, projeId, durum, limit, offset);
    }

    @GetMapping("/{isEmriId}")
    public IsEmriResponse getir(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer isEmriId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        return isEmriService.getir(firmaId, isEmriId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IsEmriResponse ekle(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @Valid @RequestBody CreateIsEmriRequest request
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);
        return isEmriService.ekle(firmaId, kullaniciId, request);
    }

    @PutMapping("/{isEmriId}")
    public IsEmriResponse guncelle(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer isEmriId,
            @Valid @RequestBody UpdateIsEmriRequest request
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);
        return isEmriService.guncelle(firmaId, isEmriId, kullaniciId, request);
    }

    @PatchMapping("/{isEmriId}/durum")
    public IsEmriResponse durumGuncelle(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer isEmriId,
            @Valid @RequestBody UpdateIsEmriDurumRequest request
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);
        return isEmriService.durumGuncelle(firmaId, isEmriId, kullaniciId, request);
    }

    @DeleteMapping("/{isEmriId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sil(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer isEmriId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        Integer kullaniciId = currentUserContext.requireUserId(currentUser);
        isEmriService.sil(firmaId, isEmriId, kullaniciId);
    }
}
