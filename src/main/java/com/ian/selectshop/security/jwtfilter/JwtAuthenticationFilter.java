package com.ian.selectshop.security.jwtfilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ian.selectshop.dto.LoginRequestDto;
import com.ian.selectshop.entity.UserRoleEnum;
import com.ian.selectshop.jwt.JwtProvider;
import com.ian.selectshop.security.UserDetailsImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * JwtFilter의 역할
 * HTTP 요청/응답 흐름에서 토큰을 꺼내고 넣는 역할
 * 응답 시 토큰을 쿠키에 담아 내려주거나, 헤더에 실어서 전달
 * <p>
 * JwtAuthenticationFilter
 * 인증 필터로, 로그인 요청이 올 때만 JWT를 새로 발급
 * UsernamePasswordAuthenticationFilter를 상속
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    // Header Key(name) 값 (Authorization: Bearer header.payload.signature)
    public static final String AUTHORIZATION_HEADER = AUTHORIZATION;
    // Bearer 인증 방식 접두사, 공백 필수
    public static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
        setFilterProcessesUrl("/api/user/login"); // 로그인 엔드포인트
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            LoginRequestDto requestDto = new ObjectMapper().readValue(request.getInputStream(), LoginRequestDto.class);

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDto.getUsername(),
                            requestDto.getPassword(),
                            null
                    )
            );
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    // 로그인 성공
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        // 응답 시 토큰을 쿠키에 담아 내려주거나, 헤더에 실어서 전달
        String username = ((UserDetailsImpl) authResult.getPrincipal()).getUsername();
        UserRoleEnum role = ((UserDetailsImpl) authResult.getPrincipal()).getUser().getRole();

        String token = jwtProvider.generateToken(username, role);

        // 헤더 전달
        response.addHeader(AUTHORIZATION_HEADER, BEARER_PREFIX + token); // Bearer 접두사 붙여서 반환
        response.addHeader("Access-Control-Expose-Headers", AUTHORIZATION_HEADER); // 브라우저 js가 읽을 수 있도록 설정

        // AJAX 여부 판단
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));

        if (isAjax) {
            // XHR: 프런트가 화면 전환
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"ok\":true}");
            response.getWriter().flush();
        } else {
            // 일반 form submit 등: 서버가 직접 리다이렉트
            response.sendRedirect("/");
        }
    }

    // 로그인 실패
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setStatus(401);
    }
}