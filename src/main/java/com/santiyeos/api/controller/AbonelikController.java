package com.santiyeos.api.controller;

import com.santiyeos.api.dto.request.BaslatAbonelikRequest;
import com.santiyeos.api.dto.response.AbonelikResponse;
import com.santiyeos.api.security.CurrentUser;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.security.CurrentUserContext;
import com.santiyeos.api.service.AbonelikService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/abonelikler")
@RequiredArgsConstructor
public class AbonelikController {

    private final AbonelikService abonelikService;
    private final CurrentUserContext currentUserContext;

    @GetMapping("/aktif")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AbonelikResponse> aktifGetir(
            @AuthenticationPrincipal CurrentUser currentUser,
            @RequestParam(required = false) Integer firmaId
    ) {
        Integer resolvedFirmaId = currentUserContext.resolveFirmaId(currentUser, firmaId);
        return ResponseEntity.ok(abonelikService.aktifGetir(resolvedFirmaId));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<PageResult<AbonelikResponse>> listele(
            @RequestParam(required = false) Integer firmaId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return ResponseEntity.ok(abonelikService.listele(firmaId, limit, offset));
    }

    @PostMapping("/firmalar/{firmaId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AbonelikResponse> baslat(
            @PathVariable Integer firmaId,
            @Valid @RequestBody BaslatAbonelikRequest request
    ) {
        AbonelikResponse response = abonelikService.baslat(firmaId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{abonelikId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> iptal(
            @PathVariable Integer abonelikId,
            @RequestParam(required = false) Integer firmaId
    ) {
        abonelikService.iptal(abonelikId, firmaId);
        return ResponseEntity.noContent().build();
    }
}