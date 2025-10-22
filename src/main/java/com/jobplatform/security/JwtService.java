package com.jobplatform.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(String username, Map<String, Object> extraClaims) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return tokenUsername.equals(username) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        String s = secret != null ? secret.trim() : "";
        if (s.isEmpty()) {
            throw new IllegalStateException("app.jwt.secret is empty or missing");
        }
        byte[] keyBytes;
        try {
            if (s.contains("-") || s.contains("_")) {
                // Likely Base64URL (URL-safe)
                keyBytes = Decoders.BASE64URL.decode(s);
            } else {
                // Standard Base64
                keyBytes = Decoders.BASE64.decode(s);
            }
        } catch (IllegalArgumentException ex) {
            // Not Base64/URL-safe: treat as raw UTF-8
            keyBytes = s.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) { // 256-bit minimum for HS256
            throw new IllegalStateException("JWT secret key too short for HS256. Provide at least 32 bytes (256-bit), raw or Base64/Base64URL-encoded.");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
