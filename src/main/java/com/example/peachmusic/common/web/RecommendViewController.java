package com.example.peachmusic.common.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class RecommendViewController {

    /**
     * 추천 페이지 이동
     */
    @GetMapping("/recommend")
    public String recommendPage() {
        return "recommend"; // templates/recommend.html
    }

}