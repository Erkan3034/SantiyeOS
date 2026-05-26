package com.santiyeos.api.controller;

import com.santiyeos.api.dto.request.CreateKullaniciRequest;
import com.santiyeos.api.dto.request.ResetKullaniciSifreRequest;
import com.santiyeos.api.dto.request.UpdateKullaniciRequest;
import com.santiyeos.api.dto.response.KullaniciResponse;
import com.santiyeos.api.exception.BusinessException;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.security.CurrentUser;
import com.santiyeos.api.security.CurrentUserContext;
import com.santiyeos.api.security.Roles;
import com.santiyeos.api.service.KullaniciService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kullanicilar")
public class KullaniciController {

    private final KullaniciService kullaniciService;
    private final CurrentUserContext currentUserContext;

    public KullaniciController(KullaniciService kullaniciService, CurrentUserContext currentUserContext) {
        this.kullaniciService = kullaniciService;
        this.currentUserContext = currentUserContext;
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN)")
    @GetMapping
    public PageResult<KullaniciResponse> listele(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) Boolean aktif,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        return kullaniciService.listele(firmaId, rol, aktif, limit, offset);
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN)")
    @GetMapping("/{kullaniciId}")
    public KullaniciResponse getir(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer kullaniciId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        return kullaniciService.getir(firmaId, kullaniciId);
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public KullaniciResponse ekle(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @Valid @RequestBody CreateKullaniciRequest request
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        validateSuperAdminRole(request.getRol());
        validateFirmaAdminRoleChange(currentUser, request.getRol());
        return kullaniciService.ekle(firmaId, request);
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN)")
    @PutMapping("/{kullaniciId}")
    public KullaniciResponse guncelle(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer kullaniciId,
            @Valid @RequestBody UpdateKullaniciRequest request
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        validateSuperAdminRole(request.getRol());
        validateFirmaAdminRoleChange(currentUser, request.getRol());

        if (currentUser.getKullaniciId().equals(kullaniciId) && Boolean.FALSE.equals(request.getAktif())) {
            throw BusinessException.badRequest("Kendi kullanıcınızı pasifleştiremezsiniz.");
        }

        return kullaniciService.guncelle(firmaId, kullaniciId, request);
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN)")
    @DeleteMapping("/{kullaniciId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sil(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer kullaniciId
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);

        if (currentUser.getKullaniciId().equals(kullaniciId)) {
            throw BusinessException.badRequest("Kendi kullanıcınızı silemezsiniz.");
        }

        kullaniciService.sil(firmaId, kullaniciId);
    }

    @PreAuthorize("hasAnyRole(T(com.santiyeos.api.security.Roles).SUPER_ADMIN, T(com.santiyeos.api.security.Roles).FIRMA_ADMIN)")
    @PatchMapping("/{kullaniciId}/sifre-resetle")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sifreResetle(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestHeader(value = "X-Firma-Id", required = false) Integer requestedFirmaId,
            @PathVariable Integer kullaniciId,
            @Valid @RequestBody ResetKullaniciSifreRequest request
    ) {
        Integer firmaId = currentUserContext.resolveFirmaId(currentUser, requestedFirmaId);
        kullaniciService.sifreResetle(firmaId, kullaniciId, request);
    }

    private void validateSuperAdminRole(String rol) {
        if (Roles.SUPER_ADMIN.equals(rol)) {
            throw BusinessException.badRequest("SUPER_ADMIN kullanıcıları bu endpoint üzerinden yönetilemez.");
        }
    }

    private void validateFirmaAdminRoleChange(CurrentUser currentUser, String rol) {
        if (!currentUser.isSuperAdmin() && Roles.FIRMA_ADMIN.equals(rol)) {
            throw BusinessException.badRequest("Firma admin başka firma admin kullanıcısı oluşturamaz veya atayamaz.");
        }
    }
}
