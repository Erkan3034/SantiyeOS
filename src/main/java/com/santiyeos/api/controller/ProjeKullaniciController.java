package com.santiyeos.api.controller;

import com.santiyeos.api.dto.request.AssignProjeKullaniciRequest;
import com.santiyeos.api.dto.response.ProjeKullaniciResponse;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.security.CurrentUser;
import com.santiyeos.api.security.CurrentUserContext;
import com.santiyeos.api.service.ProjeKullaniciService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/projeler/{projeId}/kullanicilar")
public class ProjeKullaniciController {

    private final ProjeKullaniciService projeKullaniciService;
    private final CurrentUserContext currentUserContext;

    public ProjeKullaniciController(ProjeKullaniciService projeKullaniciService, CurrentUserContext currentUserContext) {
        this.projeKullaniciService = projeKullaniciService;
        this.currentUserContext = currentUserContext;
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN)")
    @GetMapping
    public PageResult<ProjeKullaniciResponse> listele(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer projeId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        return projeKullaniciService.listele(firmaId, projeId, limit, offset);
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN)")
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ata(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer projeId,
            @Valid @RequestBody AssignProjeKullaniciRequest request
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        projeKullaniciService.ata(firmaId, projeId, request.getKullaniciId());
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN)")
    @DeleteMapping("/{kullaniciId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void kaldir(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer projeId,
            @PathVariable Integer kullaniciId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        projeKullaniciService.kaldir(firmaId, projeId, kullaniciId);
    }
}
