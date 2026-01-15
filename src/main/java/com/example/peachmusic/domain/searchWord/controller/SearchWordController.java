package com.example.peachmusic.domain.searchWord.controller;

import com.example.peachmusic.domain.searchWord.service.SearchWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SearchWordController {

    private final SearchWordService searchWordService;
}
