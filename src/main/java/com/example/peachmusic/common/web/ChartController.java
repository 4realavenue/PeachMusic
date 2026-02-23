package com.example.peachmusic.common.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChartController {

    @GetMapping("/charts")
    public String charts() {
        return "charts/charts";
    }
}
