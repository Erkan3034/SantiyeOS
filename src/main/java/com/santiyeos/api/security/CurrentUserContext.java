package com.santiyeos.api.security;

import com.santiyeos.api.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserContext {

    public Integer requireUserId(CurrentUser currentUser) {
        if (currentUser == null || currentUser.getKullaniciId() == null || currentUser.getKullaniciId() <= 0) {
            throw BusinessException.unauthorized("Geçerli kullanıcı bulunamadı.");
        }

        return currentUser.getKullaniciId();
    }

    public Integer resolveFirmaId(CurrentUser currentUser, Integer requestedFirmaId) {
        requireUserId(currentUser);

        // Firma secimini tek yerde yaparak controller'larda multi-tenant kontrol tekrarini azaltiriz.
        if (currentUser.isSuperAdmin()) {
            if (requestedFirmaId == null || requestedFirmaId <= 0) {
                throw BusinessException.badRequest("Super admin için X-Firma-Id header zorunludur.");
            }

            return requestedFirmaId;
        }

        Integer tokenFirmaId = currentUser.getFirmaId();

        if (tokenFirmaId == null || tokenFirmaId <= 0) {
            throw BusinessException.badRequest("Kullanıcının firma bilgisi bulunamadı.");
        }

        // Normal kullanici kendi token'indaki firmadan baska firmaya istek atamaz.
        if (requestedFirmaId != null && !requestedFirmaId.equals(tokenFirmaId)) {
            throw BusinessException.badRequest("Token firma bilgisi ile istek firma bilgisi eşleşmiyor.");
        }

        return tokenFirmaId;
    }


    public Integer resolveTaseronScope(CurrentUser currentUser, Integer requestedTaseronId){
        requireUserId(currentUser);

        if(!currentUser.isTaseronTemsilci()){
            return requestedTaseronId;
        }

        Integer tokenTaseronId = currentUser.getTaseronId();

        if(tokenTaseronId == null || tokenTaseronId <=0){
            throw BusinessException.badRequest("Taşeron kullanıcısının taseron bilfisi bulunamadı.");

        }

        if (requestedTaseronId != null && !requestedTaseronId.equals(tokenTaseronId)) {
            throw BusinessException.badRequest("Token taşeronn bilgisi ile istek taşeron bilgisi eşleşmiyor!");

        }

        return tokenTaseronId;
    }
}
