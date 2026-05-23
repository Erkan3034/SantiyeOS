package com.santiyeos.api.service.impl;

import com.santiyeos.api.dto.request.CreateIsEmriRaporRequest;
import com.santiyeos.api.dto.request.UpdateIsEmriRaporRequest;
import com.santiyeos.api.dto.response.IsEmriRaporResponse;
import com.santiyeos.api.exception.BusinessException;
import com.santiyeos.api.model.IsEmriRapor;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.repository.IsEmriRaporRepository;
import com.santiyeos.api.service.IsEmriRaporService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IsEmriRaporServiceImpl implements IsEmriRaporService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final IsEmriRaporRepository isEmriRaporRepository;

    public IsEmriRaporServiceImpl(IsEmriRaporRepository isEmriRaporRepository) {
        this.isEmriRaporRepository = isEmriRaporRepository;
    }

    @Override
    public PageResult<IsEmriRaporResponse> listele(Integer firmaId, Integer kullaniciId, Integer isEmriId, int limit, int offset) {
        Integer safeFirmaId = validatePositive(firmaId, "Gecerli bir firma id giriniz.");
        Integer safeKullaniciId = validatePositive(kullaniciId, "Gecerli bir kullanici id giriniz.");
        Integer safeIsEmriId = validatePositive(isEmriId, "Gecerli bir is emri id giriniz.");
        int safeLimit = normalizeLimit(limit);
        int safeOffset = Math.max(offset, 0);

        PageResult<IsEmriRapor> result = isEmriRaporRepository.listele(
                safeFirmaId,
                safeKullaniciId,
                safeIsEmriId,
                safeLimit,
                safeOffset
        );
        List<IsEmriRaporResponse> items = result.getItems().stream().map(this::toResponse).toList();

        return new PageResult<>(items, result.getTotal(), result.getLimit(), result.getOffset());
    }

    @Override
    public IsEmriRaporResponse ekle(Integer firmaId, Integer kullaniciId, Integer isEmriId, CreateIsEmriRaporRequest request) {
        Integer safeFirmaId = validatePositive(firmaId, "Gecerli bir firma id giriniz.");
        Integer safeKullaniciId = validatePositive(kullaniciId, "Gecerli bir kullanici id giriniz.");
        Integer safeIsEmriId = validatePositive(isEmriId, "Gecerli bir is emri id giriniz.");

        IsEmriRapor isEmriRapor = IsEmriRapor.builder()
                .baslik(request.getBaslik())
                .icerik(request.getIcerik())
                .build();

        Integer raporId = isEmriRaporRepository.ekle(safeFirmaId, safeKullaniciId, safeIsEmriId, isEmriRapor);

        if (raporId == null) {
            throw BusinessException.conflict("Rapor olusturuldu ancak id alinamadi.");
        }

        return getOwnedPathResponse(safeFirmaId, safeKullaniciId, safeIsEmriId, raporId);
    }

    @Override
    public IsEmriRaporResponse guncelle(
            Integer firmaId,
            Integer kullaniciId,
            Integer isEmriId,
            Integer raporId,
            UpdateIsEmriRaporRequest request
    ) {
        Integer safeFirmaId = validatePositive(firmaId, "Gecerli bir firma id giriniz.");
        Integer safeKullaniciId = validatePositive(kullaniciId, "Gecerli bir kullanici id giriniz.");
        Integer safeIsEmriId = validatePositive(isEmriId, "Gecerli bir is emri id giriniz.");
        Integer safeRaporId = validatePositive(raporId, "Gecerli bir rapor id giriniz.");

        getOwnedPathResponse(safeFirmaId, safeKullaniciId, safeIsEmriId, safeRaporId);

        IsEmriRapor isEmriRapor = IsEmriRapor.builder()
                .baslik(request.getBaslik())
                .icerik(request.getIcerik())
                .build();

        Integer etkilenenSatir = isEmriRaporRepository.guncelle(safeFirmaId, safeKullaniciId, safeRaporId, isEmriRapor);

        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("Rapor bulunamadi.");
        }

        return getOwnedPathResponse(safeFirmaId, safeKullaniciId, safeIsEmriId, safeRaporId);
    }

    @Override
    public void sil(Integer firmaId, Integer kullaniciId, Integer isEmriId, Integer raporId) {
        Integer safeFirmaId = validatePositive(firmaId, "Gecerli bir firma id giriniz.");
        Integer safeKullaniciId = validatePositive(kullaniciId, "Gecerli bir kullanici id giriniz.");
        Integer safeIsEmriId = validatePositive(isEmriId, "Gecerli bir is emri id giriniz.");
        Integer safeRaporId = validatePositive(raporId, "Gecerli bir rapor id giriniz.");

        getOwnedPathResponse(safeFirmaId, safeKullaniciId, safeIsEmriId, safeRaporId);

        Integer etkilenenSatir = isEmriRaporRepository.sil(safeFirmaId, safeKullaniciId, safeRaporId);

        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("Rapor bulunamadi.");
        }
    }

    private IsEmriRaporResponse getOwnedPathResponse(Integer firmaId, Integer kullaniciId, Integer isEmriId, Integer raporId) {
        IsEmriRapor isEmriRapor = isEmriRaporRepository.getir(firmaId, kullaniciId, raporId);

        if (isEmriRapor == null || !isEmriId.equals(isEmriRapor.getIsEmriId())) {
            throw BusinessException.notFound("Rapor bulunamadi.");
        }

        return toResponse(isEmriRapor);
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

    private IsEmriRaporResponse toResponse(IsEmriRapor isEmriRapor) {
        return IsEmriRaporResponse.builder()
                .raporId(isEmriRapor.getRaporId())
                .firmaId(isEmriRapor.getFirmaId())
                .isEmriId(isEmriRapor.getIsEmriId())
                .kullaniciId(isEmriRapor.getKullaniciId())
                .baslik(isEmriRapor.getBaslik())
                .icerik(isEmriRapor.getIcerik())
                .createdAt(isEmriRapor.getCreatedAt())
                .yazan(isEmriRapor.getYazan())
                .rol(isEmriRapor.getRol())
                .build();
    }
}
