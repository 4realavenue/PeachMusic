package com.example.peachmusic.common.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchPageController {

    @GetMapping("/search")
    public String searchPage(@RequestParam(required = false) String word,
                             Model model) {
        model.addAttribute("word", word);
        return "search/search";
    }

    @GetMapping("/search/songs")
    public String searchSongsPage(
            @RequestParam String word,
            Model model
    ) {
        model.addAttribute("word", word);
        model.addAttribute("type", "songs");
        return "search/search-detail";
    }

    @GetMapping("/search/albums")
    public String searchAlbumsPage(
            @RequestParam String word,
            Model model
    ) {
        model.addAttribute("word", word);
        model.addAttribute("type", "albums");
        return "search/search-detail";
    }

    @GetMapping("/search/artists")
    public String searchArtistsPage(
            @RequestParam String word,
            Model model
    ) {
        model.addAttribute("word", word);
        model.addAttribute("type", "artists");
        return "search/search-detail";
    }
}