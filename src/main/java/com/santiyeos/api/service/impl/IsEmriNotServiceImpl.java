package com.santiyeos.api.service.impl;

import com.santiyeos.api.dto.request.CreateIsEmriNotRequest;
import com.santiyeos.api.dto.request.UpdateIsEmriNotRequest;
import com.santiyeos.api.dto.response.IsEmriNotResponse;
import com.santiyeos.api.exception.BusinessException;
import com.santiyeos.api.model.IsEmriNot;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.repository.IsEmriNotRepository;
import com.santiyeos.api.service.IsEmriNotService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IsEmriNotServiceImpl implements IsEmriNotService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final IsEmriNotRepository isEmriNotRepository;

    public IsEmriNotServiceImpl(IsEmriNotRepository isEmriNotRepository) {
        this.isEmriNotRepository = isEmriNotRepository;
    }

    @Override
    public PageResult<IsEmriNotResponse> listele(Integer firmaId, Integer kullaniciId, Integer isEmriId, int limit, int offset) {
        Integer safeFirmaId = validatePositive(firmaId, "Gecerli bir firma id giriniz.");
        Integer safeKullaniciId = validatePositive(kullaniciId, "Gecerli bir kullanici id giriniz.");
        Integer safeIsEmriId = validatePositive(isEmriId, "Gecerli bir is emri id giriniz.");
        int safeLimit = normalizeLimit(limit);
        int safeOffset = Math.max(offset, 0);

        PageResult<IsEmriNot> result = isEmriNotRepository.listele(
                safeFirmaId,
                safeKullaniciId,
                safeIsEmriId,
                safeLimit,
                safeOffset
        );
        List<IsEmriNotResponse> items = result.getItems().stream().map(this::toResponse).toList();

        return new PageResult<>(items, result.getTotal(), result.getLimit(), result.getOffset());
    }

    @Override
    public IsEmriNotResponse ekle(Integer firmaId, Integer kullaniciId, Integer isEmriId, CreateIsEmriNotRequest request) {
        Integer safeFirmaId = validatePositive(firmaId, "Gecerli bir firma id giriniz.");
        Integer safeKullaniciId = validatePositive(kullaniciId, "Gecerli bir kullanici id giriniz.");
        Integer safeIsEmriId = validatePositive(isEmriId, "Gecerli bir is emri id giriniz.");

        IsEmriNot isEmriNot = IsEmriNot.builder()
                .icerik(request.getIcerik())
                .build();

        Integer notId = isEmriNotRepository.ekle(safeFirmaId, safeKullaniciId, safeIsEmriId, isEmriNot);

        if (notId == null) {
            throw BusinessException.conflict("Not olusturuldu ancak id alinamadi.");
        }

        return getOwnedPathResponse(safeFirmaId, safeKullaniciId, safeIsEmriId, notId);
    }

    @Override
    public IsEmriNotResponse guncelle(
            Integer firmaId,
            Integer kullaniciId,
            Integer isEmriId,
            Integer notId,
            UpdateIsEmriNotRequest request
    ) {
        Integer safeFirmaId = validatePositive(firmaId, "Gecerli bir firma id giriniz.");
        Integer safeKullaniciId = validatePositive(kullaniciId, "Gecerli bir kullanici id giriniz.");
        Integer safeIsEmriId = validatePositive(isEmriId, "Gecerli bir is emri id giriniz.");
        Integer safeNotId = validatePositive(notId, "Gecerli bir not id giriniz.");

        getOwnedPathResponse(safeFirmaId, safeKullaniciId, safeIsEmriId, safeNotId);

        IsEmriNot isEmriNot = IsEmriNot.builder()
                .icerik(request.getIcerik())
                .build();

        Integer etkilenenSatir = isEmriNotRepository.guncelle(safeFirmaId, safeKullaniciId, safeNotId, isEmriNot);

        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("Not bulunamadi.");
        }

        return getOwnedPathResponse(safeFirmaId, safeKullaniciId, safeIsEmriId, safeNotId);
    }

    @Override
    public void sil(Integer firmaId, Integer kullaniciId, Integer isEmriId, Integer notId) {
        Integer safeFirmaId = validatePositive(firmaId, "Gecerli bir firma id giriniz.");
        Integer safeKullaniciId = validatePositive(kullaniciId, "Gecerli bir kullanici id giriniz.");
        Integer safeIsEmriId = validatePositive(isEmriId, "Gecerli bir is emri id giriniz.");
        Integer safeNotId = validatePositive(notId, "Gecerli bir not id giriniz.");

        getOwnedPathResponse(safeFirmaId, safeKullaniciId, safeIsEmriId, safeNotId);

        Integer etkilenenSatir = isEmriNotRepository.sil(safeFirmaId, safeKullaniciId, safeNotId);

        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("Not bulunamadi.");
        }
    }

    private IsEmriNotResponse getOwnedPathResponse(Integer firmaId, Integer kullaniciId, Integer isEmriId, Integer notId) {
        IsEmriNot isEmriNot = isEmriNotRepository.getir(firmaId, kullaniciId, notId);

        if (isEmriNot == null || !isEmriId.equals(isEmriNot.getIsEmriId())) {
            throw BusinessException.notFound("Not bulunamadi.");
        }

        return toResponse(isEmriNot);
    }

    private Integer validatePositive(Integer value, String message) {
        if (value == null || value <= 0) {
            throw BusinessException.badRequest(message);
        }

        return value;
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(limit, MAX_LIMIT);
    }

    private IsEmriNotResponse toResponse(IsEmriNot isEmriNot) {
        return IsEmriNotResponse.builder()
                .notId(isEmriNot.getNotId())
                .firmaId(isEmriNot.getFirmaId())
                .isEmriId(isEmriNot.getIsEmriId())
                .kullaniciId(isEmriNot.getKullaniciId())
                .icerik(isEmriNot.getIcerik())
                .createdAt(isEmriNot.getCreatedAt())
                .yazan(isEmriNot.getYazan())
                .rol(isEmriNot.getRol())
                .build();
    }
}
