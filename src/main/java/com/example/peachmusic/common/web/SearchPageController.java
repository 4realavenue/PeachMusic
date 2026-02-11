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

    @GetMapping("/search/artists")
    public String artistsPage(@RequestParam String word, Model model) {
        model.addAttribute("word", word);
        return "search/artists";
    }

    @GetMapping("/search/albums")
    public String albumsPage(@RequestParam String word, Model model) {
        model.addAttribute("word", word);
        return "search/albums";
    }

    @GetMapping("/search/songs")
    public String songsPage(@RequestParam String word, Model model) {
        model.addAttribute("word", word);
        return "search/songs";
    }

}