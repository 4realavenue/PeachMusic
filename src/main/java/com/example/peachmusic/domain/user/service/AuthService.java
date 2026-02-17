package com.example.peachmusic.domain.user.service;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.enums.UserRole;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.filter.JwtUtil;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.mail.service.MailService;
import com.example.peachmusic.domain.user.dto.request.LoginRequestDto;
import com.example.peachmusic.domain.user.dto.request.UserCreateRequestDto;
import com.example.peachmusic.domain.user.dto.response.LoginResponseDto;
import com.example.peachmusic.domain.user.dto.response.UserCreateResponseDto;
import com.example.peachmusic.domain.user.entity.User;
import com.example.peachmusic.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final MailService mailService;


    /**
     * 회원가입
     */
    @Transactional
    public UserCreateResponseDto createUser(UserCreateRequestDto request) {

        if (!mailService.isEmailVerified(request.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(ErrorCode.USER_EXIST_NICKNAME);
        }

        String encodePassword = passwordEncoder.encode(request.getPassword());

        User user = new User(request.getName(), request.getNickname(), request.getEmail(), encodePassword);
        userRepository.save(user);
        return UserCreateResponseDto.from(user);
    }

    /**
     * 로그인
     */
    @Transactional
    public LoginResponseDto login(LoginRequestDto request) {
        User user = userRepository.findUserByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.AUTH_INVALID_PASSWORD);
        }

        String token = jwtUtil.createToken(user.getUserId(), user.getEmail(), user.getRole(), user.getTokenVersion());
        AuthUser authUser = new AuthUser( user.getUserId(), user.getEmail(), user.getRole(), user.getTokenVersion());
        UserRole role = user.getRole();

        Authentication authentication = new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return new LoginResponseDto(token, role);
    }

    /**
     *  로그아웃
     */
    @Transactional
    public void logout(AuthUser authUser) {
        userService.findUser(authUser).increaseTokenVersion();
    }
}