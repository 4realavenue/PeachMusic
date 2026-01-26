package com.example.peachmusic.domain.user.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.domain.user.dto.request.VerifyCodeRequest;
import com.example.peachmusic.domain.user.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/email")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/verify-code")
    public ResponseEntity<CommonResponse<Void>> verifyCode(
            @RequestBody @Valid VerifyCodeRequest request
    ) {
        memberService.verifyEmailCode(request.email(), request.code());
        return ResponseEntity.ok(CommonResponse.success("이메일 인증이 완료되었습니다."));
    }
}

