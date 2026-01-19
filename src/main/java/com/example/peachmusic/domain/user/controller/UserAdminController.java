package com.example.peachmusic.domain.user.controller;

import com.example.peachmusic.common.enums.UserRole;
import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.common.model.PageResponse;
import com.example.peachmusic.domain.user.model.request.UserRoleChangeRequest;
import com.example.peachmusic.domain.user.model.response.admin.UserAdminGetResponse;
import com.example.peachmusic.domain.user.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class UserAdminController {

    private final UserAdminService useradminService;

    // 전체조회
    @GetMapping
    public ResponseEntity<PageResponse<UserAdminGetResponse>> getUsers(
            @PageableDefault(
                    size = 10,
                    page = 0
            ) Pageable pageable
    ) {
        PageResponse<UserAdminGetResponse> response = useradminService.getAllUser(pageable);

        return ResponseEntity.ok(response);
    }

    // 유저 삭제
    @DeleteMapping("/{userId}")
    public ResponseEntity deleteUser(
            @PathVariable Long userId
    ) {
        useradminService.deleteUser(userId);

        CommonResponse response = new CommonResponse<>(true, "유저 비활성화 성공", null);

        return ResponseEntity.ok(response);
    }

    // 유저 복구
    @PatchMapping("/{userId}/restore")
    public ResponseEntity updateUser(
            @PathVariable Long userId
    ) {
        useradminService.restorationUser(userId);

        CommonResponse response = new CommonResponse<>(true, "유저 활성화 성공", null);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<CommonResponse<Void>> changeRole(
            @PathVariable Long userId,
            @RequestBody UserRoleChangeRequest request
    ) {
        UserRole newRole = request.getRole();
        useradminService.role(userId, newRole);

        CommonResponse<Void> response = new CommonResponse<>(
                true,
                "계정 권한 변경 성공",
                null
        );

        return ResponseEntity.ok(response);
    }
}
