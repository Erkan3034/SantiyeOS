package com.santiyeos.api.service.impl;

import com.santiyeos.api.dto.request.CreateMalzemeKategoriRequest;
import com.santiyeos.api.dto.request.UpdateMalzemeKategoriRequest;
import com.santiyeos.api.dto.response.MalzemeKategoriResponse;
import com.santiyeos.api.exception.BusinessException;
import com.santiyeos.api.model.MalzemeKategori;
import com.santiyeos.api.model.PageResult;
import com.santiyeos.api.repository.MalzemeKategoriRepository;
import com.santiyeos.api.service.MalzemeKategoriService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class MalzemeKategoriServiceImpl implements MalzemeKategoriService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;
    private static final Set<String> YAZMA_ROLLERI = Set.of("SUPER_ADMIN", "FIRMA_ADMIN", "PROJE_YONETICISI");

    private final MalzemeKategoriRepository kategoriRepository;

    public MalzemeKategoriServiceImpl(MalzemeKategoriRepository kategoriRepository) {
        this.kategoriRepository = kategoriRepository;
    }

    @Override
    public PageResult<MalzemeKategoriResponse> listele(Integer firmaId, int limit, int offset) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        int safeLimit = normalizeLimit(limit);
        int safeOffset = Math.max(offset, 0);

        PageResult<MalzemeKategori> result = kategoriRepository.listele(safeFirmaId, safeLimit, safeOffset);
        List<MalzemeKategoriResponse> items = result.getItems().stream().map(this::toResponse).toList();

        return new PageResult<>(items, result.getTotal(), result.getLimit(), result.getOffset());
    }

    @Override
    public MalzemeKategoriResponse getir(Integer firmaId, Integer kategoriId) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeKategoriId = validatePositive(kategoriId, "Gecerli bir kategori id giriniz.");

        MalzemeKategori kategori = kategoriRepository.getir(safeFirmaId, safeKategoriId);

        if (kategori == null) {
            throw BusinessException.notFound("Malzeme kategorisi bulunamadi.");
        }

        return toResponse(kategori);
    }

    @Override
    public MalzemeKategoriResponse ekle(Integer firmaId, String rol, CreateMalzemeKategoriRequest request) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        String safeRol = validateWriteRol(rol);

        MalzemeKategori kategori = MalzemeKategori.builder()
                .firmaId(safeFirmaId)
                .ad(request.getAd())
                .aciklama(request.getAciklama())
                .build();

        Integer kategoriId = kategoriRepository.ekle(safeFirmaId, safeRol, kategori);

        if (kategoriId == null) {
            throw BusinessException.conflict("Malzeme kategorisi olusturuldu ancak id alinamadi.");
        }

        return getir(safeFirmaId, kategoriId);
    }

    @Override
    public MalzemeKategoriResponse guncelle(
            Integer firmaId,
            Integer kategoriId,
            String rol,
            UpdateMalzemeKategoriRequest request
    ) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeKategoriId = validatePositive(kategoriId, "Gecerli bir kategori id giriniz.");
        String safeRol = validateWriteRol(rol);

        MalzemeKategori kategori = MalzemeKategori.builder()
                .ad(request.getAd())
                .aciklama(request.getAciklama())
                .build();

        Integer etkilenenSatir = kategoriRepository.guncelle(safeFirmaId, safeKategoriId, safeRol, kategori);

        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("Malzeme kategorisi bulunamadi.");
        }

        return getir(safeFirmaId, safeKategoriId);
    }

    @Override
    public void sil(Integer firmaId, Integer kategoriId, Integer kullaniciId, String rol) {
        Integer safeFirmaId = validateFirmaId(firmaId);
        Integer safeKategoriId = validatePositive(kategoriId, "Gecerli bir kategori id giriniz.");
        Integer safeKullaniciId = validatePositive(kullaniciId, "Gecerli bir kullanici id giriniz.");
        String safeRol = validateWriteRol(rol);

        Integer etkilenenSatir = kategoriRepository.sil(safeFirmaId, safeKategoriId, safeKullaniciId, safeRol);

        if (etkilenenSatir == null || etkilenenSatir == 0) {
            throw BusinessException.notFound("Malzeme kategorisi bulunamadi.");
        }
    }

    private Integer validateFirmaId(Integer firmaId) {
        return validatePositive(firmaId, "Gecerli bir firma id giriniz.");
    }

    private Integer validatePositive(Integer value, String message) {
        if (value == null || value <= 0) {
            throw BusinessException.badRequest(message);
        }

        return value;
    }

    private String validateWriteRol(String rol) {
        if (rol == null || rol.isBlank()) {
            throw BusinessException.unauthorized("Gecerli kullanici rolu bulunamadi.");
        }

        String normalized = rol.trim().toUpperCase(Locale.ROOT);

        if (!YAZMA_ROLLERI.contains(normalized)) {
            throw BusinessException.forbidden("Bu islem icin yetkiniz yok.");
        }

        return normalized;
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(limit, MAX_LIMIT);
    }

    private MalzemeKategoriResponse toResponse(MalzemeKategori kategori) {
        return MalzemeKategoriResponse.builder()
                .kategoriId(kategori.getKategoriId())
                .firmaId(kategori.getFirmaId())
                .ad(kategori.getAd())
                .aciklama(kategori.getAciklama())
                .aktif(kategori.getAktif())
                .createdAt(kategori.getCreatedAt())
                .malzemeSayisi(kategori.getMalzemeSayisi())
                .build();
    }
}
