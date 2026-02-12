package com.example.peachmusic.common.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AlbumPageController {

    @GetMapping("/albums/{albumId}/page")
    public String albumDetailPage(@PathVariable Long albumId, Model model) {
        model.addAttribute("albumId", albumId);
        return "album/album-detail";
    }
}
