package com.example.peachmusic.domain.jamendo.service;

import com.example.peachmusic.domain.jamendo.dto.JamendoSongResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class JamendoApiService {
    private final RestClient jamendoRestClient;

    @Value("${jamendo.api.key}")
    private String clientId;

    @PostConstruct
    public void checkKey() {
        System.out.println("JAMENDO KEY = " + clientId);
    }

    public JamendoSongResponse initJamendo(int page, int limit, String type) {
        return jamendoRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tracks")
                        .queryParam("client_id", clientId)
                        .queryParam("format", "json")
                        .queryParam("include", "musicinfo")
                        .queryParam("vocalinstrumental", type)
                        .queryParam("datebetween", "2024-01-01_2026-01-19")
                        .queryParam("order", "releasedate_asc")
                        .queryParam("limit", limit)
                        .queryParam("offset", (page - 1) * limit)
                        .build())
                .retrieve()
                .body(JamendoSongResponse.class);
    }

    public JamendoSongResponse scheduleJamendo(int page, int limit, String datebetween) {
        return jamendoRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tracks")
                        .queryParam("client_id", clientId)
                        .queryParam("format", "json")
                        .queryParam("include", "musicinfo")
                        .queryParam("datebetween", datebetween)
                        .queryParam("order", "releasedate_desc")
                        .queryParam("limit", limit)
                        .queryParam("offset", (page - 1) * limit)
                        .build())
                .retrieve()
                .body(JamendoSongResponse.class);
    }
}