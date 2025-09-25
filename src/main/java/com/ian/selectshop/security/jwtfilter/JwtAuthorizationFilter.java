package com.ian.selectshop.security.jwtfilter;

import com.ian.selectshop.jwt.JwtProvider;
import com.ian.selectshop.security.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * JwtFilter의 역할
 * HTTP 요청/응답 흐름에서 토큰을 꺼내고 넣는 역할
 * 1. 쿠키/세션/헤더 등 다른 저장소에서 토큰 꺼냄
 * 2.Authorization 헤더에서 Bearer 접두사 제거 (토큰 추출)
 * 3. 추출한 토큰을 JwtProvider로 검증
 * 4. 검증 성공 시 SecurityContext에 인증 정보(UserDetails 등) 저장
 * <p>
 * JwtAuthorizationFilter
 * 인가 필터로, 로그인 이후 모든 보호된 API 호출마다 JWT 검증 및 반환
 * OncePerRequestFilter를 상속해 모든 요청에서 한 번만 실행
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    // Header Key(name) 값 (Authorization: Bearer header.payload.signature)
    public static final String AUTHORIZATION_HEADER = AUTHORIZATION;
    // Bearer 인증 방식 접두사, 공백 필수
    public static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            Claims claims = validateToken(token);
            if (claims != null) {
                setAuthentication(claims, request);
            }
        }

        filterChain.doFilter(request, response);
    }


    // 1. 요청 시 쿠키/세션/헤더 등 다른 저장소에서 토큰 꺼냄
    private String resolveToken(HttpServletRequest request) {
        // 2. Authorization 헤더에서 Bearer 접두사 제거 (토큰 추출)
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX))
            return header.substring(BEARER_PREFIX.length());

        return null;
    }

    // 3. 추출한 토큰을 JwtProvider에서 검증
    private Claims validateToken(String token) {
        if (jwtProvider.validateToken(token))
            return jwtProvider.parseToken(token);

        return null;
    }

    // 4. 검증 성공 시 SecurityContext에 인증 정보(UserDetails 등) 저장
    private void setAuthentication(Claims claims, HttpServletRequest request) {
        String username = claims.getSubject();

        if (!StringUtils.hasText(username)) return;
        if (SecurityContextHolder.getContext().getAuthentication() != null) return;

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}