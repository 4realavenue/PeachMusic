package com.example.peachmusic.common.filter;

import com.example.peachmusic.common.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.rmi.ServerException;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final Long TOKEN_TIME = 10 * 60 * 1000L; // 60분

    @Value("${jwt.secret.key}")
    private String secretKey; // 야물 키
    private Key key; // 키
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256; // 알고리즘

    @PostConstruct // 체크한다.
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey); // Base64인지 체크하겠다.
        key = Keys.hmacShaKeyFor(bytes);
    }

    public String substringToken(String tokenValue) throws ServerException { // 토큰에 Bearer 떼서 토큰 키만 받겠다.
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(TOKEN_PREFIX)) { // 토큰값이 tokenValue 또는 TOKEN_PREFIX ("bearer ")을 잘라내서 받겠다..
            return tokenValue.substring(7); // ("bearer " 7자 날림)
        }
        throw new ServerException("Not Found Token");
    }
    // 토큰 복호화 위 클레임즈값을 전부 추출한다 [ email이랑 UserRole 추출하겠다. ] 값 꺼내 쓰겠다.
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 토큰 생성
    public String createToken(Long userId, String email, UserRole role, Long tokenVersion) {
        Date date = new Date();

        return TOKEN_PREFIX +
                Jwts.builder()
                        .setSubject(String.valueOf(userId))
                        .claim("email", email) // 토큰에 닉네임 넣겠다 (토큰 커스텀)
                        .claim("userRole", role != null ? role.name() : UserRole.USER.name())// 토큰에 유저롤 넣겠다 (토큰 커스텀)
                        .claim("version", tokenVersion)  // 버전 정보 추가
                        .setExpiration(new Date(date.getTime() + TOKEN_TIME))// 현재일자 + 만료시간 (1시간) ex) 오후 2시 -> 2시 + 1시간 = 3시 만료
                        .setIssuedAt(date)// 발급일 알려줌
                        .signWith(key, signatureAlgorithm) // 암호화 알고리즘
                        .compact();
    }
}
