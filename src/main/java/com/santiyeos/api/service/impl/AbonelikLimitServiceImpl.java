package com.santiyeos.api.service.impl;

import com.santiyeos.api.exception.BusinessException;
import com.santiyeos.api.repository.AbonelikLimitRepository;
import com.santiyeos.api.service.AbonelikLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AbonelikLimitServiceImpl implements AbonelikLimitService {

    private static final String PROJE = "PROJE";
    private static final String KULLANICI = "KULLANICI";
    private static final String TASERON = "TASERON";

    private final AbonelikLimitRepository abonelikLimitRepository;

    @Override
    public void projeEklemeHakkiKontrolEt(Integer firmaId) {
        limitKontrolEt(firmaId, PROJE, "Proje limiti doldu. Lütfen abonelik planınızı yükseltin.");
    }

    @Override
    public void kullaniciEklemeHakkiKontrolEt(Integer firmaId) {
        limitKontrolEt(firmaId, KULLANICI, "Kullanıcı limiti doldu. Lütfen abonelik planınızı yükseltin.");
    }

    @Override
    public void taseronEklemeHakkiKontrolEt(Integer firmaId) {
        limitKontrolEt(firmaId, TASERON, "Taşeron limiti doldu. Lütfen abonelik planınızı yükseltin.");
    }

    private void limitKontrolEt(Integer firmaId, String kaynakTipi, String limitMesaji) {
        if (firmaId == null || firmaId <= 0) {
            throw BusinessException.badRequest("Geçerli bir firma id giriniz.");
        }

        if (!abonelikLimitRepository.aktifAbonelikVarMi(firmaId)) {
            throw BusinessException.forbidden("Aktif abonelik bulunamadı.");
        }

        int limit = abonelikLimitRepository.planLimit(firmaId, kaynakTipi);
        int kullanim = abonelikLimitRepository.kullanimSayisi(firmaId, kaynakTipi);

        if (limit <= 0) {
            throw BusinessException.forbidden("Abonelik plan limiti geçersiz.");
        }

        if (kullanim >= limit) {
            throw BusinessException.conflict(limitMesaji);
        }
    }
}