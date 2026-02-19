package com.example.peachmusic.common.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class SongPageController {

    @GetMapping("/songs/{songId}/page")
    public String songDetailPage(@PathVariable Long songId, Model model) {
        model.addAttribute("songId", songId);
        return "song/song-detail";
    }
}

