package com.santiyeos.api.controller;

import com.santiyeos.api.dto.request.CreateProjeRequest;
import com.santiyeos.api.dto.request.UpdateProjeRequest;
import com.santiyeos.api.dto.response.ProjeResponse;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.service.ProjeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projeler")
public class ProjeController {

    private final ProjeService projeService;

    public ProjeController(ProjeService projeService) {
        this.projeService = projeService;
    }

    @GetMapping
    public PageResult<ProjeResponse> listele(
            @RequestHeader("X-Firma-Id") Integer firmaId,
            @RequestParam(required = false) String durum,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return projeService.listele(firmaId, durum, limit, offset);
    }

    @GetMapping("/{projeId}")
    public ProjeResponse getir(
            @RequestHeader("X-Firma-Id") Integer firmaId,
            @PathVariable Integer projeId
    ) {
        return projeService.getir(firmaId, projeId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjeResponse ekle(
            @RequestHeader("X-Firma-Id") Integer firmaId,
            @Valid @RequestBody CreateProjeRequest request
    ) {
        return projeService.ekle(firmaId, request);
    }

    @PutMapping("/{projeId}")
    public ProjeResponse guncelle(
            @RequestHeader("X-Firma-Id") Integer firmaId,
            @PathVariable Integer projeId,
            @Valid @RequestBody UpdateProjeRequest request
    ) {
        return projeService.guncelle(firmaId, projeId, request);
    }

    @DeleteMapping("/{projeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sil(
            @RequestHeader("X-Firma-Id") Integer firmaId,
            @RequestHeader("X-Kullanici-Id") Integer kullaniciId,
            @PathVariable Integer projeId
    ) {
        projeService.sil(firmaId, projeId, kullaniciId);
    }
}
