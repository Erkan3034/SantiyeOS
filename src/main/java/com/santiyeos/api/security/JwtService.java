package com.santiyeos.api.security;

import com.santiyeos.api.model.Kullanici;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final Duration expiration;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = Duration.ofMinutes(expirationMinutes);
    }

    public String createAccessToken(Kullanici kullanici) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(String.valueOf(kullanici.getKullaniciId()))
                .claim("firmaId", kullanici.getFirmaId())
                .claim("taseronId", kullanici.getTaseronId())
                .claim("ad", kullanici.getAd())
                .claim("soyad", kullanici.getSoyad())
                .claim("email", kullanici.getEmail())
                .claim("rol", kullanici.getRol())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public CurrentUser parseCurrentUser(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return CurrentUser.builder()
                .kullaniciId(Integer.valueOf(claims.getSubject()))
                .firmaId(toInteger(claims.get("firmaId")))
                .taseronId(toInteger(claims.get("taseronId")))
                .ad(claims.get("ad", String.class))
                .soyad(claims.get("soyad", String.class))
                .email(claims.get("email", String.class))
                .rol(claims.get("rol", String.class))
                .build();
    }

    public long getExpirationSeconds() {
        return expiration.toSeconds();
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        return Integer.valueOf(value.toString());
    }
}
