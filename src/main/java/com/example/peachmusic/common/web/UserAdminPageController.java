package com.example.peachmusic.common.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/users")
class UserAdminPageController {

    /**
     * 관리자 회원 관리 페이지
     */
    @GetMapping
    public String adminUsersPage() {
        return "admin/admin-users";
    }
}