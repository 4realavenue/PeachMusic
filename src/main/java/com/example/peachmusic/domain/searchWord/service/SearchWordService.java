package com.example.peachmusic.domain.searchWord.service;

import com.example.peachmusic.domain.searchWord.repository.SearchWordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchWordService {

    private final SearchWordRepository searchWordRepository;
}
