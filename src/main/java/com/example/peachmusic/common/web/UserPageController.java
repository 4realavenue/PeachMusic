package com.example.peachmusic.common.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserPageController {

    /**
     * 마이페이지 메인
     */
    @GetMapping("/mypage")
    public String mypage(Model model) {
        model.addAttribute("title", "My Page");
        return "mypage/mypage";
    }

    /**
     * 내가 좋아요한 앨범 페이지
     */
    @GetMapping("/likes/albums")
    public String likedAlbumsPage() {
        return "mypage/liked-albums";
    }

    /**
     * 내가 좋아요한 노래 페이지
     */
    @GetMapping("/likes/songs")
    public String likedSongsPage() {
        return "mypage/liked-songs";
    }

    /**
     * 내가 좋아요한 아티스트 페이지
     */
    @GetMapping("/likes/artists")
    public String likedArtistsPage() {
        return "mypage/liked-artists";
    }
}
