package com.santiyeos.api.service.impl;

import com.santiyeos.api.dto.request.CreateTaseronRequest;
import com.santiyeos.api.dto.response.TaseronResponse;
import com.santiyeos.api.exception.BusinessException;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.model.Taseron;
import com.santiyeos.api.repository.TaseronRepository;
import com.santiyeos.api.service.TaseronService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaseronServiceImpl implements TaseronService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final TaseronRepository taseronRepository;

    public TaseronServiceImpl(TaseronRepository taseronRepository) {
        this.taseronRepository = taseronRepository;
    }

    @Override
    public PageResult<TaseronResponse> listele(Integer firmaId, int limit, int offset) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        int safeLimit = normalizeLimit(limit);
        int safeOffset = Math.max(offset, 0);

        PageResult<Taseron> result = taseronRepository.listele(safeFirmaId, safeLimit, safeOffset);

        List<TaseronResponse> responseItems = result.getItems()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageResult<>(
                responseItems,
                result.getTotal(),
                result.getLimit(),
                result.getOffset()
        );
    }

    @Override
    public TaseronResponse getir(Integer firmaId, Integer taseronId) {
        Integer safeFirmaId = validateFirmaId(firmaId);

        if (taseronId == null || taseronId <= 0) {
            throw BusinessException.badRequest("Geçerli bir taşeron id giriniz.");
        }

        Taseron taseron = taseronRepository.getir(safeFirmaId, taseronId);

        if (taseron == null) {
            throw BusinessException.notFound("Taşeron bulunamadı.");
        }

        return toResponse(taseron);
    }

    @Override
    public TaseronResponse ekle(Integer firmaId, CreateTaseronRequest request) {
        Integer safeFirmaId = validateFirmaId(firmaId);

        Taseron taseron = Taseron.builder()
                .firmaId(safeFirmaId)
                .ad(request.getAd())
                .vergiNo(request.getVergiNo())
                .yetkiliAd(request.getYetkiliAd())
                .telefon(request.getTelefon())
                .email(request.getEmail())
                .uzmanlik(request.getUzmanlik())
                .build();

        Integer taseronId = taseronRepository.ekle(safeFirmaId, taseron);

        if (taseronId == null) {
            throw BusinessException.conflict("Taşeron oluşturuldu ancak id alınamadı.");
        }

        return getir(safeFirmaId, taseronId);
    }

    private Integer validateFirmaId(Integer firmaId) {
        if (firmaId == null || firmaId <= 0) {
            throw BusinessException.badRequest("Geçerli bir firma id giriniz.");
        }

        return firmaId;
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(limit, MAX_LIMIT);
    }

    private TaseronResponse toResponse(Taseron taseron) {
        return TaseronResponse.builder()
                .taseronId(taseron.getTaseronId())
                .firmaId(taseron.getFirmaId())
                .ad(taseron.getAd())
                .vergiNo(taseron.getVergiNo())
                .yetkiliAd(taseron.getYetkiliAd())
                .telefon(taseron.getTelefon())
                .email(taseron.getEmail())
                .uzmanlik(taseron.getUzmanlik())
                .performansSkoru(taseron.getPerformansSkoru())
                .aktif(taseron.getAktif())
                .createdAt(taseron.getCreatedAt())
                .updatedAt(taseron.getUpdatedAt())
                .build();
    }
}
