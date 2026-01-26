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

    @GetMapping("/admin/users")
    public ResponseEntity<PageResponse<UserAdminGetResponseDto>> getUsers(
            @RequestParam(required = false) String word,
            @PageableDefault(size = 10, page = 0, sort = "userId") Pageable pageable
    ) {
        Page<UserAdminGetResponseDto> response = useradminService.getAllUser(word, pageable);

        return ResponseEntity.ok(PageResponse.success("유저 목록 조회를 성공했습니다.", response));
    }

    @DeleteMapping("/admin/users/{userId}/delete")
    public ResponseEntity deleteUser(
            @PathVariable Long userId
    ) {
        useradminService.deleteUser(userId);

        return ResponseEntity.ok(CommonResponse.success("유저 비활성화를 성공했습니다."));
    }

    @PatchMapping("/admin/users/{userId}/restore")
    public ResponseEntity updateUser(
            @PathVariable Long userId
    ) {
        useradminService.restorationUser(userId);

        return ResponseEntity.ok(CommonResponse.success("유저 활성화를 성공했습니다."));
    }

    @PatchMapping("/admin/users/{userId}/role")
    public ResponseEntity<CommonResponse<Void>> changeRole(
            @PathVariable Long userId,
            @RequestBody UserRoleChangeRequestDto request
    ) {
        UserRole newRole = request.getRole();

        useradminService.role(userId, newRole);

        return ResponseEntity.ok(CommonResponse.success("계정 권한 변경을 완료했습니다." ));
    }
}
