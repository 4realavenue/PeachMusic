package com.example.peachmusic.common.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class SongAdminPageController {

    @GetMapping("/songs")
    public String adminSongs() {
        return "admin/songs"; // 목록 페이지(원하면 나중에 연결)
    }

    @GetMapping("/songs/create")
    public String adminSongCreatePage() {
        return "admin/admin-song-create";
    }
}

