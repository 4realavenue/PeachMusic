package com.example.peachmusic.common.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class JamendoAdminPageController {

    /**
     * Jamendo 데이터 관리 페이지
     */
    @GetMapping("/admin/openapi/jamendo")
    public String jamendoPage() {
        return "admin/admin-jamendo";
    }
}