package com.example.peachmusic.domain.user.service;

import com.example.peachmusic.common.enums.UserRole;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.filter.JwtUtil;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.user.entity.User;
import com.example.peachmusic.domain.user.dto.request.LoginRequestDto;
import com.example.peachmusic.domain.user.dto.request.UserCreateRequestDto;
import com.example.peachmusic.domain.user.dto.request.UserUpdateRequestDto;
import com.example.peachmusic.domain.user.dto.response.LoginResponseDto;
import com.example.peachmusic.domain.user.dto.response.UserCreateResponseDto;
import com.example.peachmusic.domain.user.dto.response.UserGetResponseDto;
import com.example.peachmusic.domain.user.dto.response.admin.UserUpdateResponseDto;
import com.example.peachmusic.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
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
    private final JwtUtil jwtUtil;
    private final MemberService memberService;

    @Transactional
    public UserCreateResponseDto createUser(@Valid UserCreateRequestDto request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.USER_EXIST_EMAIL);
        }

        String encodePassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .name(request.getName())
                .nickname(request.getNickname())
                .email(request.getEmail())
                .password(encodePassword)
                .role(UserRole.USER)
                .emailVerified(false)
                .tokenVersion(0L)
                .build();


        userRepository.save(user);

        // 이메일 발송
        memberService.sendCodeToEmail(request.getEmail());

        return UserCreateResponseDto.from(user);
    }


    @Transactional(readOnly = true)
    public UserGetResponseDto getUser(AuthUser authUser) {
        return UserGetResponseDto.from(authUser.getUser());
    }


    @Transactional
    public UserUpdateResponseDto update(@Valid UserUpdateRequestDto request, AuthUser authUser) {

        User user = authUser.getUser();

        if (isNotBlank(request.getNickname()) && !request.getNickname().trim().equals(user.getNickname())) {
            if (userRepository.existsByNickname(request.getNickname().trim())) {
                throw new CustomException(ErrorCode.USER_EXIST_NICKNAME);
            }
        }

        user.update(request);

        return UserUpdateResponseDto.from(user);
    }

    @Transactional
    public void deleteUser(AuthUser authUser) {
        authUser.getUser().delete();
    }

    @Transactional
    public LoginResponseDto login(@Valid LoginRequestDto request) {
        User user = userRepository.findUserByEmailAndIsDeletedFalse(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.AUTH_INVALID_PASSWORD);
        }
        String token = jwtUtil.createToken(user.getUserId(), user.getEmail(), user.getRole(), user.getTokenVersion());

        AuthUser authUser = new AuthUser(user, user.getUserId(), user.getEmail(), user.getRole(), user.getTokenVersion());

        Authentication authentication = new UsernamePasswordAuthenticationToken(authUser,null, authUser.getAuthoritie());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return new LoginResponseDto(token);
    }

    @Transactional
    public void logout(AuthUser authUser) {
        authUser.getUser().increaseTokenVersion();
    }

}
