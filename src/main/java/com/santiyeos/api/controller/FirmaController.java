package com.santiyeos.api.controller;

import com.santiyeos.api.dto.request.CreateFirmaRequest;
import com.santiyeos.api.dto.request.UpdateFirmaRequest;
import com.santiyeos.api.dto.response.FirmaResponse;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.service.FirmaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/firmalar")
@RequiredArgsConstructor
public class FirmaController {

    private final FirmaService firmaService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<PageResult<FirmaResponse>> listele(
            @RequestParam(required = false) Boolean aktif,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return ResponseEntity.ok(firmaService.listele(aktif, limit, offset));
    }

    @GetMapping("/{firmaId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @currentUserContext.isSameFirma(authentication.principal, #firmaId)")
    public ResponseEntity<FirmaResponse> getir(@PathVariable Integer firmaId) {
        return ResponseEntity.ok(firmaService.getir(firmaId));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<FirmaResponse> ekle(@Valid @RequestBody CreateFirmaRequest request) {
        FirmaResponse response = firmaService.ekle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{firmaId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or @currentUserContext.isSameFirma(authentication.principal, #firmaId)")
    public ResponseEntity<FirmaResponse> guncelle(
            @PathVariable Integer firmaId,
            @Valid @RequestBody UpdateFirmaRequest request
    ) {
        return ResponseEntity.ok(firmaService.guncelle(firmaId, request));
    }

    @DeleteMapping("/{firmaId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> pasiflestir(@PathVariable Integer firmaId) {
        firmaService.pasiflestir(firmaId);
        return ResponseEntity.noContent().build();
    }
}