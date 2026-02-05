package com.example.peachmusic.domain.worker.service;

import com.example.peachmusic.domain.worker.dto.request.WorkerRetryRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class WorkerService {

    @Value("${worker.base-url}")
    private String workerBaseUrl;

    /**
     * (Worker) 다운로드 재시도 요청
     */
    @Transactional
    public void retryDownloadSong(WorkerRetryRequestDto requestDto) {

        WebClient webClient = WebClient.builder()
                .baseUrl(workerBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        webClient.method(HttpMethod.POST)
                .uri("/worker/re-download")
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    /**
     * (Worker) 형변환 재시도 요청
     */
    @Transactional
    public void retryTranscodeSong(WorkerRetryRequestDto requestDto) {

        WebClient webClient = WebClient.builder()
                .baseUrl(workerBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        webClient.method(HttpMethod.POST)
                .uri("/worker/re-transcode")
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
}
