package com.example.peachmusic.domain.jamendo.service;

import com.example.peachmusic.domain.jamendo.model.JamendoSongResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class JamendoApiService {

    private final RestClient jamendoRestClient;

    @Value("${jamendo.api.key}")
    private String clientId;

    public JamendoSongResponse fetchSongs(int page, int limit, String datebetween) {
        long start = System.nanoTime();

        JamendoSongResponse response = jamendoRestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder
                            .path("/tracks")
                            .queryParam("client_id", clientId)
                            .queryParam("format", "json")
                            .queryParam("include", "musicinfo")
                            .queryParam("order", "id_desc")
                            .queryParam("limit", limit)
                            .queryParam("offset", (page - 1) * limit);

                            if(datebetween != null) {
                                uriBuilder.queryParam("datebetween", datebetween);
                            }

                            return uriBuilder.build();
                })
                .retrieve()
                .body(JamendoSongResponse.class);

        long end = System.nanoTime();
        long elapsedMs = (end - start) / 1_000_000;

        log.info("[Jamendo API] fetchSongs page={}, limit={} → {} ms", page, limit, elapsedMs);

        return response;
    }
}