package com.example.peachmusic.common.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/forbidden")
    public String forbidden() {
        return "error/403";
    }

    @GetMapping("/signup")
    public String signup() {
        return "auth/signup";
    }

    // ===== 관리자 화면 =====
    @GetMapping("/admin")
    public String adminHome() {
        return "admin/admin"; // templates/admin/index.html
    }

    @GetMapping("/admin/artists")
    public String adminArtists() {
        return "admin/artists"; // templates/admin/artists.html
    }

    @GetMapping("/admin/albums")
    public String adminAlbums() {
        return "admin/albums"; // templates/admin/albums.html
    }

    @GetMapping("/admin/songs")
    public String adminSongs() {
        return "admin/songs"; // templates/admin/songs.html
    }

    @GetMapping("/admin/users")
    public String adminUsers() {
        return "admin/users"; // templates/admin/users.html
    }
}
