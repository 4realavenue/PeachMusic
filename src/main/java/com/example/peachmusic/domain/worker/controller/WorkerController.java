package com.example.peachmusic.domain.worker.controller;

import com.example.peachmusic.domain.worker.dto.request.WorkerRetryRequestDto;
import com.example.peachmusic.domain.worker.service.WorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class WorkerController {

    private final WorkerService streamingJobService;

    /**
     * (Worker) 음원 다운로드 재시도
     */
    @PostMapping("/admin/songs/re-download")
    public void retryDownloadSong(
            @RequestBody WorkerRetryRequestDto requestDto
    ) {
        streamingJobService.retryDownloadSong(requestDto);
    }

    /**
     * (Worker) 음원 형변환 재시도
     */
    @PostMapping("/admin/songs/re-transcode")
    public void retryTranscodeSong(
            @RequestBody WorkerRetryRequestDto requestDto
    ) {
        streamingJobService.retryTranscodeSong(requestDto);
    }
}
