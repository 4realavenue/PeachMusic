package com.example.peachmusic.domain.user.controller;

import com.example.peachmusic.common.enums.UserRole;
import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.common.model.PageResponse;
import com.example.peachmusic.domain.user.dto.request.UserRoleChangeRequestDto;
import com.example.peachmusic.domain.user.dto.response.admin.UserAdminGetResponseDto;
import com.example.peachmusic.domain.user.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/")
public class UserAdminController {

    private final UserAdminService useradminService;

    // 전체조회
    @GetMapping("/admin/users")
    public ResponseEntity<PageResponse<UserAdminGetResponseDto>> getUsers(
            @RequestParam(required = false) String word,
            @PageableDefault(size = 10, page = 0, sort = "userId") Pageable pageable
    ) {
        Page<UserAdminGetResponseDto> response = useradminService.getAllUser(word, pageable);

        return ResponseEntity.ok(PageResponse.success("전체 유저 조회 성공", response));
    }

    // 유저 삭제
    @DeleteMapping("/admin/users/{userId}/delete")
    public ResponseEntity deleteUser(
            @PathVariable Long userId
    ) {
        useradminService.deleteUser(userId);

        return ResponseEntity.ok(CommonResponse.success("계정 삭제 성공"));
    }

    // 유저 복구
    @PatchMapping("/admin/users/{userId}/restore")
    public ResponseEntity updateUser(
            @PathVariable Long userId
    ) {
        useradminService.restorationUser(userId);

        return ResponseEntity.ok(CommonResponse.success("계정 복구 성공"));
    }

    @PatchMapping("/admin/users/{userId}/role")
    public ResponseEntity<CommonResponse<Void>> changeRole(
            @PathVariable Long userId,
            @RequestBody UserRoleChangeRequestDto request
    ) {
        UserRole newRole = request.getRole();

        useradminService.role(userId, newRole);

        return ResponseEntity.ok(CommonResponse.success("계정 권한 변경 성공" ));
    }
}
