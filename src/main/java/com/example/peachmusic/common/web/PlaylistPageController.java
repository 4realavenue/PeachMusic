package com.example.peachmusic.common.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PlaylistPageController {

    @GetMapping("/playlists")
    public String playlistPage() {
        return "playlists/my-playlists";
    }
}
