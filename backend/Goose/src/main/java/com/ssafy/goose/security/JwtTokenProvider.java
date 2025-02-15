package com.ssafy.goose.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.security.Key;

@Component
public class JwtTokenProvider {

    private final Key secretKey;
    private final long accessTokenValidity;
    private final long refreshTokenValidity;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.access-expiration:3600000}") long accessValidity,
                            @Value("${jwt.refresh-expiration:1209600000}") long refreshValidity) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenValidity = accessValidity;
        this.refreshTokenValidity = refreshValidity;
    }

    // AccessToken 생성
    public String createAccessToken(String username) {
        return createToken(username, accessTokenValidity);
    }

    // RefreshToken 생성
    public String createRefreshToken(String username) {
        return createToken(username, refreshTokenValidity);
    }

    private String createToken(String username, long validity) {
        Claims claims = Jwts.claims().setSubject(username);
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validity);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰에서 사용자 식별자(username) 추출
    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            return !claims.getBody().getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
