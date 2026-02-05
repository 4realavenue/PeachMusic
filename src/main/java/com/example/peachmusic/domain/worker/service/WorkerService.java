package com.example.peachmusic.domain.worker.service;

import com.example.peachmusic.domain.worker.dto.request.WorkerTryRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerService {

    @Value("${worker.base-url}")
    private String workerBaseUrl;

    /**
     * (Worker) 다운로드 재시도 요청
     */
    public void tryDownloadSong(WorkerTryRequestDto requestDto) {

        WebClient webClient = WebClient.builder()
                .baseUrl(workerBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        webClient.method(HttpMethod.POST)
                .uri("/worker/songs/download")
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe();
    }

    /**
     * (Worker) 형변환 재시도 요청
     */
    public void tryTranscodeSong(WorkerTryRequestDto requestDto) {

        WebClient webClient = WebClient.builder()
                .baseUrl(workerBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        webClient.method(HttpMethod.POST)
                .uri("/worker/songs/transcode")
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe();
    }
}
