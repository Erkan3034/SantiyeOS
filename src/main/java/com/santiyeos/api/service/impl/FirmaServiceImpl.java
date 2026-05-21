package com.santiyeos.api.service.impl;

import com.santiyeos.api.dto.request.CreateFirmaRequest;
import com.santiyeos.api.dto.request.UpdateFirmaRequest;
import com.santiyeos.api.dto.response.FirmaResponse;
import com.santiyeos.api.exception.BusinessException;
import com.santiyeos.api.model.Firma;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.repository.FirmaRepository;
import com.santiyeos.api.service.FirmaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FirmaServiceImpl implements FirmaService {

    private static final int MAX_LIMIT = 100;

    private final FirmaRepository firmaRepository;

    @Override
    public PageResult<FirmaResponse> listele(Boolean aktif, int limit, int offset) {
        int safeLimit = normalizeLimit(limit);
        int safeOffset = normalizeOffset(offset);

        PageResult<Firma> result = firmaRepository.listele(aktif, safeLimit, safeOffset);
        List<FirmaResponse> items = result.getItems()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageResult<>(items, result.getTotal(), result.getLimit(), result.getOffset());
    }

    @Override
    public FirmaResponse getir(Integer firmaId) {
        Integer safeFirmaId = validatePositive(firmaId, "Geçerli bir firma id giriniz.");

        Firma firma = firmaRepository.getir(safeFirmaId);
        if (firma == null) {
            throw BusinessException.notFound("Firma bulunamadı.");
        }

        return toResponse(firma);
    }

    @Override
    public FirmaResponse ekle(CreateFirmaRequest request) {
        Firma firma = Firma.builder()
                .ad(request.getAd())
                .vergiNo(request.getVergiNo())
                .telefon(request.getTelefon())
                .email(request.getEmail())
                .adres(request.getAdres())
                .build();

        Integer firmaId = firmaRepository.ekle(firma);
        if (firmaId == null) {
            throw BusinessException.conflict("Firma oluşturulamadı.");
        }

        return getir(firmaId);
    }

    @Override
    public FirmaResponse guncelle(Integer firmaId, UpdateFirmaRequest request) {
        Integer safeFirmaId = validatePositive(firmaId, "Geçerli bir firma id giriniz.");

        Firma firma = Firma.builder()
                .ad(request.getAd())
                .vergiNo(request.getVergiNo())
                .telefon(request.getTelefon())
                .email(request.getEmail())
                .adres(request.getAdres())
                .aktif(request.getAktif())
                .build();

        Integer etkilenenSatir = firmaRepository.guncelle(safeFirmaId, firma);
        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("Firma bulunamadı.");
        }

        return getir(safeFirmaId);
    }

    @Override
    public void pasiflestir(Integer firmaId) {
        Integer safeFirmaId = validatePositive(firmaId, "Geçerli bir firma id giriniz.");

        Integer etkilenenSatir = firmaRepository.pasiflestir(safeFirmaId);
        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("Firma bulunamadı.");
        }
    }

    private FirmaResponse toResponse(Firma firma) {
        return FirmaResponse.builder()
                .firmaId(firma.getFirmaId())
                .ad(firma.getAd())
                .vergiNo(firma.getVergiNo())
                .telefon(firma.getTelefon())
                .email(firma.getEmail())
                .adres(firma.getAdres())
                .aktif(firma.getAktif())
                .createdAt(firma.getCreatedAt())
                .updatedAt(firma.getUpdatedAt())
                .build();
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return 20;
        }

        return Math.min(limit, MAX_LIMIT);
    }

    private int normalizeOffset(int offset) {
        return Math.max(offset, 0);
    }

    private Integer validatePositive(Integer value, String message) {
        if (value == null || value <= 0) {
            throw BusinessException.badRequest(message);
        }

        return value;
    }
}