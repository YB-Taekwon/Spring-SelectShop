package com.ian.selectshop.jwt;

import com.ian.selectshop.entity.UserRoleEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

/**
 * JwtProvider (JwtUtil)의 역할
 * 토큰 자체와 관련된 기능만 담당
 * 1. JWT 생성
 * 2. JWT 파싱 (정보 꺼내기)
 * 3. JWT 검증
 */
@Slf4j
@Component
public class JwtProvider {
    // JWT 데이터
    // 사용자 권한 값의 KEY (roles: "ROLE_ADMIN")
    public static final String AUTHORIZATION_KEY = "roles";
    // 토큰 만료 시간 (60분)
    private final Duration TOKEN_TIME = Duration.ofHours(1); // Duration 클래스
    // JWT Secret Key
    @Value("${JWT-SECRET-KEY}")
    private String secretKeyStr; // JWT Secret Key (보통 Base64로 인코딩 된 일반 문자열)
    private SecretKey secretKey; // 실제 암호화 알고리즘에 사용할 SecretKey 객체 (init 메서드를 통해 디코딩하여 주입)
    // JWT를 만들 때 사용할 해시 알고리즘
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    // JWT 서명/검증에 필요한 key 객체를 준비하는 초기화 로직
    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKeyStr);
        secretKey = Keys.hmacShaKeyFor(bytes);
    }

    // 1. JWT 생성
    public String generateToken(String username, UserRoleEnum role) {
        Instant now = Instant.now();
        Instant expiry = now.plus(TOKEN_TIME);

        return Jwts.builder()
                .setSubject(username) // 사용자 식별자값(ID)
                .claim(AUTHORIZATION_KEY, role) // 사용자 권한
                .setIssuedAt(Date.from(now)) // 발급일
                .setExpiration(Date.from(expiry)) // 만료 시간
                .signWith(secretKey, signatureAlgorithm) // 알고리즘에서 사용할 key, 암호화 알고리즘
                .compact();
    }

    // 2. JWT 파싱
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey) // JwtParserBuilder에 서명을 검증 할 키 지정
                .build()
                .parseSignedClaims(token) // 지정한 키를 기반으로 서명 검증 및 파싱
                .getPayload(); // payload(claims) 추출
    }

    // 3. JWT 검증
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (SecurityException | MalformedJwtException | SignatureException e) {
            log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token, 만료된 JWT token 입니다.: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.: {}", e.getMessage());
        }
        return false;
    }
}