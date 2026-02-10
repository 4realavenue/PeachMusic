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

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerService {

    private final WebClient webClient;

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(6);

    /**
     * (Worker) 다운로드 재시도 요청
     */
    public void tryDownloadSong(WorkerTryRequestDto requestDto) {

        webClient.method(HttpMethod.POST)
                .uri("/worker/songs/download-request")
                .bodyValue(requestDto)
                .retrieve()
                .toBodilessEntity()
                .doOnError(exception -> log.error("Worker 불러오기 실패 : {}", exception.getMessage()))
                .subscribe();
    }

    /**
     * (Worker) 형변환 재시도 요청
     */
    public void tryTranscodeSong(WorkerTryRequestDto requestDto) {

        webClient.method(HttpMethod.POST)
                .uri("/worker/songs/transcode-request")
                .bodyValue(requestDto)
                .retrieve()
                .toBodilessEntity()
                .doOnError(exception -> log.error("Worker 불러오기 실패 : {}", exception.getMessage()))
                .subscribe();
    }
}
