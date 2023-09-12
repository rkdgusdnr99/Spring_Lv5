package com.example.postlv3.jwt;

import com.example.postlv3.dto.StatusResponseDto;
import com.example.postlv3.entity.UserRoleEnum;
import com.example.postlv3.security.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j(topic = "JWT 검증 및 인가")
//authfilter,loggingfilter 대신 편리하게 사용
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthorizationFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain) throws ServletException, IOException {

        String accessTokenValue = jwtUtil.getTokenFromRequest(req);

        // 오류 메세지
        StatusResponseDto responseDto = new StatusResponseDto("토큰이 유효하지 않습니다.", 400);
        // 응답 데이터 설정
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        // JSON 변환 후 출력
        ObjectMapper objectMapper = new ObjectMapper();

        if (StringUtils.hasText(accessTokenValue)) {
            // JWT 토큰 substring
            accessTokenValue = jwtUtil.substringToken(accessTokenValue);
            log.info(accessTokenValue);

            if (!jwtUtil.validateToken(accessTokenValue)) {
                String refreshTokenValue = jwtUtil.getJwtFromHeader(req);
                System.out.println("refershToken 값: " + refreshTokenValue);
                if (!jwtUtil.validateToken(refreshTokenValue)) {
                    log.error("Token Error");
                    System.out.println("Token Error");
                    objectMapper.writeValue(res.getWriter(), responseDto);
                    return;
                }
                else {
                    Claims refreshTokenInfo = jwtUtil.getUserInfoFromToken(refreshTokenValue);
                    String newAccessToken = jwtUtil.createAccessToken(refreshTokenInfo.getSubject(), refreshTokenInfo.get("role", UserRoleEnum.class));
                    jwtUtil.addJwtToCookie(newAccessToken, res);
                    accessTokenValue = jwtUtil.substringToken(newAccessToken);
                }

            }

            Claims info = jwtUtil.getUserInfoFromToken(accessTokenValue);

            try {
                setAuthentication(info.getSubject());
            } catch (Exception e) {
                log.error(e.getMessage());
                objectMapper.writeValue(res.getWriter(), responseDto);
                return;
            }
        }

        filterChain.doFilter(req, res);
    }

    // 인증 처리
    public void setAuthentication(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(username);
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

    // 인증 객체 생성
    private Authentication createAuthentication(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}