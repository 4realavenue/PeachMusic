package com.example.peachmusic.common.filter;

import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.user.entity.User;
import com.example.peachmusic.domain.user.repository.UserRepository;
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
    private final UserRepository userRepository;

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
            Claims claims = jwtUtil.extractClaims(jwt);

            Long userId = Long.parseLong(claims.getSubject());
            Long tokenVersion = claims.get("version", Long.class);

            if (tokenVersion == null) {
                throw new BadCredentialsException("토큰 버전 정보가 없습니다.");
            }

            // DB에서 최신 버전 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BadCredentialsException("사용자를 찾을 수 없습니다."));

            // 버전이 다르다면, 로그아웃된 토큰 출력
            if (!user.getTokenVersion().equals(tokenVersion)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"message\":\"만료된 토큰입니다.\"}");
                return;
            }

            // 버전이 같으면 정상 인증 진행함
            String email = (String) claims.get("email");
            String roleStr = claims.get("userRole", String.class);
            UserRole role = (roleStr != null) ? UserRole.valueOf(roleStr) : UserRole.USER;

            AuthUser authuser = new AuthUser(userId, email, role);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    authuser, null, authuser.getAuthoritie()
            );

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