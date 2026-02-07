package com.example.peachmusic.domain.worker.controller;

import com.example.peachmusic.domain.worker.dto.request.WorkerTryRequestDto;
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
    @PostMapping("/admin/songs/download")
    public void tryDownloadSong(
            @RequestBody WorkerTryRequestDto requestDto
    ) {
        streamingJobService.tryDownloadSong(requestDto);
    }

    /**
     * (Worker) 음원 형변환 재시도
     */
    @PostMapping("/admin/songs/transcode")
    public void tryTranscodeSong(
            @RequestBody WorkerTryRequestDto requestDto
    ) {
        streamingJobService.tryTranscodeSong(requestDto);
    }
}
