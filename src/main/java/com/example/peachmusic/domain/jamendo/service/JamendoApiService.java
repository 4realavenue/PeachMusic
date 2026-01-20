package com.example.peachmusic.domain.jamendo.service;

import com.example.peachmusic.domain.jamendo.dto.JamendoSongResponse;
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

    // 병목현상이 여긴가? 확인해보기 시간 측정
    public JamendoSongResponse fetchSongs(int page, int limit, String type, String datebetween) {
        return jamendoRestClient.get()
                .uri(uriBuilder -> {
                    uriBuilder
                            .path("/tracks")
                            .queryParam("client_id", clientId)
                            .queryParam("format", "json")
                            .queryParam("include", "musicinfo")
                            .queryParam("order", "releasedate_desc")
                            .queryParam("limit", limit)
                            .queryParam("offset", (page - 1) * limit);

                            if(type != null){
                                uriBuilder.queryParam("vocalinstrumental", type);
                            }

                            if(datebetween != null) {
                                uriBuilder.queryParam("datebetween", datebetween);
                            }

                            return uriBuilder.build();
                })
                .retrieve()
                .body(JamendoSongResponse.class);
    }
}