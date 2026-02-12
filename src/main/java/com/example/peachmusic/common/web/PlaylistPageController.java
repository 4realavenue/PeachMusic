package com.example.peachmusic.common.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PlaylistPageController {

    @GetMapping("/playlists")
    public String playlistPage() {
        return "playlists/my-playlists";
    }

    @GetMapping("/playlists/{playlistId}")
    public String playlistDetailPage(@PathVariable Long playlistId,
                                     Model model) {
        model.addAttribute("playlistId", playlistId);
        return "playlists/playlist-detail";
    }

}
