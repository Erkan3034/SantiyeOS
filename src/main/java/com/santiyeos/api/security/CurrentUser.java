package com.santiyeos.api.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrentUser {

    private Integer kullaniciId;
    private Integer firmaId;
    private Integer taseronId;
    private String ad;
    private String soyad;
    private String email;
    private String rol;

    public List<GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(rol);
    }
}
