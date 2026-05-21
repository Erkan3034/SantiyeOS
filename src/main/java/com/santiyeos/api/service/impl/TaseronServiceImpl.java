package com.santiyeos.api.service.impl;

import com.santiyeos.api.dto.request.CreateTaseronRequest;
import com.santiyeos.api.dto.request.UpdateTaseronRequest;
import com.santiyeos.api.dto.response.TaseronResponse;
import com.santiyeos.api.exception.BusinessException;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.model.Taseron;
import com.santiyeos.api.repository.TaseronRepository;
import com.santiyeos.api.service.TaseronService;
import org.springframework.stereotype.Service;
import com.santiyeos.api.service.AbonelikLimitService;
import java.util.List;

@Service
public class TaseronServiceImpl implements TaseronService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final TaseronRepository taseronRepository;
    private final AbonelikLimitService abonelikLimitService;
    public TaseronServiceImpl(TaseronRepository taseronRepository, AbonelikLimitService abonelikLimitService) {
        this.taseronRepository = taseronRepository;
        this.abonelikLimitService = abonelikLimitService;
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

        Integer safeTaseronId = validateTaseronId(taseronId);

        Taseron taseron = taseronRepository.getir(safeFirmaId, safeTaseronId);

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
        abonelikLimitService.taseronEklemeHakkiKontrolEt(safeFirmaId);
        Integer taseronId = taseronRepository.ekle(safeFirmaId, taseron);

        if (taseronId == null) {
            throw BusinessException.conflict("Taşeron oluşturuldu ancak id alınamadı.");
        }

        return getir(safeFirmaId, taseronId);
    }

    @Override
    public TaseronResponse guncelle(Integer firmaId, Integer taseronId, UpdateTaseronRequest request) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeTaseronId = validateTaseronId(taseronId);

        Taseron taseron = Taseron.builder()
                .firmaId(safeFirmaId)
                .taseronId(safeTaseronId)
                .ad(request.getAd())
                .vergiNo(request.getVergiNo())
                .yetkiliAd(request.getYetkiliAd())
                .telefon(request.getTelefon())
                .email(request.getEmail())
                .uzmanlik(request.getUzmanlik())
                .build();

        Integer etkilenenSatir = taseronRepository.guncelle(safeFirmaId, safeTaseronId, taseron);

        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("Taşeron bulunamadı.");
        }

        return getir(safeFirmaId, safeTaseronId);
    }



    @Override
    public void sil(Integer firmaId, Integer taseronId, Integer kullaniciId) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeTaseronId = validateTaseronId(taseronId);
        Integer safeKullaniciId = validateKullaniciId(kullaniciId);

        Integer etkilenenSatir = taseronRepository.sil(safeFirmaId, safeTaseronId, safeKullaniciId);

        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("Taşeron bulunamadı.");
        }
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

    private Integer validateTaseronId(Integer taseronId) {
        if (taseronId == null || taseronId <= 0) {
            throw BusinessException.badRequest("Geçerli bir taşeron id giriniz.");
        }

        return taseronId;
    }

    private Integer validateKullaniciId(Integer kullaniciId) {
        if (kullaniciId == null || kullaniciId <= 0) {
            throw BusinessException.badRequest("Geçerli bir kullanıcı id giriniz.");
        }

        return kullaniciId;
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
