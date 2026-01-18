package com.example.peachmusic.domain.user.controller;

import com.example.peachmusic.common.model.CommonResponse;
import com.example.peachmusic.common.model.PageResponse;
import com.example.peachmusic.domain.user.model.response.admin.UserAdminGetResponse;
import com.example.peachmusic.domain.user.service.UserAdminService;
import com.example.peachmusic.domain.user.service.UserService;
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


}
