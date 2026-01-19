package com.example.peachmusic.common.filter;

import com.example.peachmusic.common.model.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.peachmusic.common.enums.UserRole;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String bearerJwt = request.getHeader("Authorization");

        if (bearerJwt == null || bearerJwt.isBlank()) { // 토큰이 null 이거나 비어있을경우
            // 토큰이 없는 경우 시큐리티한테 위임
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = jwtUtil.substringToken(bearerJwt);

        try {
            // JWT 유효성 검사와 claims 추출
            Claims claims = jwtUtil.extractClaims(jwt);
            if (claims == null) {
                filterChain.doFilter(request, response);
                return;
            }

            log.info("claims = {}", claims);

            String subject = claims.getSubject();
            if(subject == null){
                throw new BadCredentialsException("JWT에 userId(sub)가 없습니다.");
            }

            Long userId = Long.parseLong(subject);
            String email = (String) claims.get("email");

            String roleStr = claims.get("userRole", String.class);
            UserRole role = (roleStr != null) ? UserRole.valueOf(roleStr) : UserRole.USER;

            AuthUser authuser = new AuthUser(userId, email, role);
            Authentication authentication = new UsernamePasswordAuthenticationToken(authuser, null, authuser.getAuthoritie());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.", e);
            throw new BadCredentialsException("유효하지 않는 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token, 만료된 JWT token 입니다.", e);
            throw new CredentialsExpiredException("만료된 JWT 서명입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.", e);
            throw new BadCredentialsException("지원되지 않는 JWT 서명입니다.");
        } catch (Exception e) {
            log.error("Internal server error", e);
            throw new AuthenticationServiceException("인증 처리 중 서버 JWT 서명입니다.", e);
        }
    }
}