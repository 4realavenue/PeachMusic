package com.example.peachmusic.domain.openapi.scheduler;

import com.example.peachmusic.domain.openapi.jamendo.service.JamendoSongService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenApiScheduler {

    private final JamendoSongService jamendoSongService;

    /**
     * Jamendo 음원 매일 3시 정기 적재
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void importScheduledJamendo() {
        jamendoSongService.importScheduledJamendo();
    }

    @Scheduled(cron = "0 30 3 1 * ?")
    public void importJamendoMonthlySync() {
        jamendoSongService.importJamendoMonthlySync();
    }
}