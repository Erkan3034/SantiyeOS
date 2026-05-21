package com.santiyeos.api.service.impl;

import com.santiyeos.api.dto.request.BaslatAbonelikRequest;
import com.santiyeos.api.dto.response.AbonelikResponse;
import com.santiyeos.api.exception.BusinessException;
import com.santiyeos.api.model.Abonelik;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.repository.AbonelikRepository;
import com.santiyeos.api.service.AbonelikService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AbonelikServiceImpl implements AbonelikService {

    private static final int MAX_LIMIT = 100;

    private final AbonelikRepository abonelikRepository;

    @Override
    public AbonelikResponse aktifGetir(Integer firmaId) {
        Integer safeFirmaId = validatePositive(firmaId, "Geçerli bir firma id giriniz.");

        Abonelik abonelik = abonelikRepository.aktifGetir(safeFirmaId);
        if (abonelik == null) {
            throw BusinessException.notFound("Aktif abonelik bulunamadı.");
        }

        return toResponse(abonelik);
    }

    @Override
    public PageResult<AbonelikResponse> listele(Integer firmaId, int limit, int offset) {
        Integer safeFirmaId = firmaId == null ? null : validatePositive(firmaId, "Geçerli bir firma id giriniz.");
        int safeLimit = normalizeLimit(limit);
        int safeOffset = normalizeOffset(offset);

        PageResult<Abonelik> result = abonelikRepository.listele(safeFirmaId, safeLimit, safeOffset);
        List<AbonelikResponse> items = result.getItems()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageResult<>(items, result.getTotal(), result.getLimit(), result.getOffset());
    }

    @Override
    public AbonelikResponse baslat(Integer firmaId, BaslatAbonelikRequest request) {
        Integer safeFirmaId = validatePositive(firmaId, "Geçerli bir firma id giriniz.");
        Integer safePlanId = validatePositive(request.getPlanId(), "Geçerli bir abonelik plan id giriniz.");

        if (!request.getBitisTarihi().isAfter(request.getBaslangicTarihi())) {
            throw BusinessException.badRequest("Abonelik bitiş tarihi başlangıç tarihinden sonra olmalıdır.");
        }

        Integer abonelikId = abonelikRepository.baslat(
                safeFirmaId,
                safePlanId,
                request.getBaslangicTarihi(),
                request.getBitisTarihi(),
                request.getDeneme()
        );

        if (abonelikId == null) {
            throw BusinessException.conflict("Abonelik başlatılamadı.");
        }

        return aktifGetir(safeFirmaId);
    }

    @Override
    public void iptal(Integer abonelikId, Integer firmaId) {
        Integer safeAbonelikId = validatePositive(abonelikId, "Geçerli bir abonelik id giriniz.");
        Integer safeFirmaId = firmaId == null ? null : validatePositive(firmaId, "Geçerli bir firma id giriniz.");

        Integer etkilenenSatir = abonelikRepository.iptal(safeAbonelikId, safeFirmaId);
        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("Aktif abonelik bulunamadı.");
        }
    }

    private AbonelikResponse toResponse(Abonelik abonelik) {
        return AbonelikResponse.builder()
                .abonelikId(abonelik.getAbonelikId())
                .firmaId(abonelik.getFirmaId())
                .firmaAd(abonelik.getFirmaAd())
                .planId(abonelik.getPlanId())
                .planAd(abonelik.getPlanAd())
                .maxProje(abonelik.getMaxProje())
                .maxKullanici(abonelik.getMaxKullanici())
                .maxTaseron(abonelik.getMaxTaseron())
                .aylikUcret(abonelik.getAylikUcret())
                .baslangicTarihi(abonelik.getBaslangicTarihi())
                .bitisTarihi(abonelik.getBitisTarihi())
                .durum(abonelik.getDurum())
                .deneme(abonelik.getDeneme())
                .createdAt(abonelik.getCreatedAt())
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