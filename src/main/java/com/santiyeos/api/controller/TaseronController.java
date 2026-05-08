package com.santiyeos.api.controller;

import com.santiyeos.api.dto.request.CreateTaseronRequest;
import com.santiyeos.api.dto.response.TaseronResponse;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.service.TaseronService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.santiyeos.api.dto.request.UpdateTaseronRequest;
@RestController
@RequestMapping("/api/taseronlar")
public class TaseronController {

    private final TaseronService taseronService;

    public TaseronController(TaseronService taseronService) {
        this.taseronService = taseronService;
    }

    @GetMapping
    public PageResult<TaseronResponse> listele(
            @RequestHeader("X-Firma-Id") Integer firmaId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return taseronService.listele(firmaId, limit, offset);
    }

    @GetMapping("/{taseronId}")
    public TaseronResponse getir(
            @RequestHeader("X-Firma-Id") Integer firmaId,
            @PathVariable Integer taseronId
    ) {
        return taseronService.getir(firmaId, taseronId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaseronResponse ekle(
            @RequestHeader("X-Firma-Id") Integer firmaId,
            @Valid @RequestBody CreateTaseronRequest request
    ) {
        return taseronService.ekle(firmaId, request);
    }


    @PutMapping("/{taseronId}")
    public TaseronResponse guncelle(
            @RequestHeader("X-Firma-Id") Integer firmaId,
            @PathVariable Integer taseronId,
            @Valid @RequestBody UpdateTaseronRequest request
    ) {
        return taseronService.guncelle(firmaId, taseronId, request);
    }

    @DeleteMapping("/{taseronId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sil(
            @RequestHeader("X-Firma-Id") Integer firmaId,
            @RequestHeader("X-Kullanici-Id") Integer kullaniciId,
            @PathVariable Integer taseronId
    ) {
        taseronService.sil(firmaId, taseronId, kullaniciId);
    }

}
