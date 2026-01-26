package com.example.peachmusic.domain.user.service;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.filter.JwtUtil;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.email.service.MailCheckService;
import com.example.peachmusic.domain.user.dto.request.LoginRequestDto;
import com.example.peachmusic.domain.user.dto.request.UserCreateRequestDto;
import com.example.peachmusic.domain.user.dto.request.UserUpdateRequestDto;
import com.example.peachmusic.domain.user.dto.response.LoginResponseDto;
import com.example.peachmusic.domain.user.dto.response.UserGetResponseDto;
import com.example.peachmusic.domain.user.dto.response.admin.UserUpdateResponseDto;
import com.example.peachmusic.domain.user.entity.User;
import com.example.peachmusic.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailCheckService mailCheckService;
    private final JwtUtil jwtUtil;

    /**
     * 회원가입
     */
    @Transactional
    public void createUser(UserCreateRequestDto request) {

        if (!mailCheckService.isEmailVerified(request.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.USER_EXIST_EMAIL);
        }

        if (userRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(ErrorCode.USER_EXIST_NICKNAME);
        }


        String encodePassword = passwordEncoder.encode(request.getPassword());


        User user = new User(request.getName(), request.getNickname(), request.getEmail(), encodePassword);
        userRepository.save(user);
    }

    /**
     * 내 정보 조회
     */
    @Transactional(readOnly = true)
    public UserGetResponseDto getUser(AuthUser authUser) {
        return UserGetResponseDto.from(authUser.getUser());
    }

    /**
     *  내 정보 수정
     */
    @Transactional
    public UserUpdateResponseDto update(UserUpdateRequestDto request, AuthUser authUser) {
        User user = authUser.getUser();

        if (isNotBlank(request.getNickname()) && !request.getNickname().trim().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname().trim())) {
                throw new CustomException(ErrorCode.USER_EXIST_NICKNAME);
            }
        }

        user.update(request);
        return UserUpdateResponseDto.from(user);
    }

    /**
     *  비활성화
     */
    @Transactional
    public void deleteUser(AuthUser authUser) {
        authUser.getUser().delete();
    }

    /**
     *  로그인
     */
    @Transactional
    public LoginResponseDto login(LoginRequestDto request) {
        User user = userRepository.findUserByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.AUTH_INVALID_PASSWORD);
        }

        String token = jwtUtil.createToken(user.getUserId(), user.getEmail(), user.getRole(), user.getTokenVersion());
        AuthUser authUser = new AuthUser(user, user.getUserId(), user.getEmail(), user.getRole(), user.getTokenVersion());

        Authentication authentication = new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthoritie());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return new LoginResponseDto(token);
    }

    /**
     *  로그아웃
     */
    @Transactional
    public void logout(AuthUser authUser) {
        authUser.getUser().increaseTokenVersion();
    }
}
