package com.example.peachmusic.domain.worker.controller;

import com.example.peachmusic.domain.worker.dto.request.WorkerRetryRequestDto;
import com.example.peachmusic.domain.worker.service.WorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class WorkerController {

    private final WorkerService streamingJobService;

    @PostMapping("/songs/re-download")
    public void retryDownloadSong(
            @RequestBody WorkerRetryRequestDto requestDto
            ) {
        streamingJobService.retryWorkSong(requestDto);
    }

    @PostMapping("/songs/re-transcode")
    public void retryTranscodeSong(
            @RequestBody WorkerRetryRequestDto requestDto
    ) {
        streamingJobService.retryWorkSong(requestDto);
    }
}
