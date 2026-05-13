package com.chump.auth.service;

import com.chump.user.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${auth.jwt.secret-key}")
    private String secretKey;

    @Value("${auth.jwt.expiration-time}")
    private long expirationTime;

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        // Сериализуем для хранения
        claims.put("scopes", user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList());

        return createToken(user, claims);
    }

    private String createToken(User user, Map<String, Object> claims) {
        Date now = new Date();
        Date expirationTime = new Date(now.getTime() + this.expirationTime);

        String token = Jwts.builder()
                .subject(user.getId().toString())
                .signWith(getSigningKey())
                .claims(claims)
                .issuedAt(now)
                .expiration(expirationTime)
                .compact();

        log.info("Successfully created token for user with id: {}", user.getId());

        return token;
    }

    public Collection<? extends GrantedAuthority> getScopes(String token) {
        List<?> authorities = extractClaim(token, o -> o.get("scopes", List.class));
        // Десериализуем
        return authorities
                .stream()
                .map(Object::toString)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    public int getUserId(String token) {
        return Integer.parseInt(extractClaim(token, Claims::getSubject));
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        Claims claims = extractClaims(token);
        return claimResolver.apply(claims);
    }

    private SecretKey getSigningKey() {
        byte[] bytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }
}
