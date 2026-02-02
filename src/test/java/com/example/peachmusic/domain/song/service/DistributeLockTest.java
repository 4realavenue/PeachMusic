package com.example.peachmusic.domain.song.service;

import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.repository.SongRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles("test")
@SpringBootTest()
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DistributeLockTest {

    @Autowired
    private SongRepository songRepository;
    @Autowired
    private SongService songService;
    @Autowired
    private AlbumRepository albumRepository;

    @Test
    void redis_분산락_테스트() throws InterruptedException {

        Album album = albumRepository.save(new Album( "테스트 앨범", LocalDate.of(2024, 1, 1), "https://image.test/" + UUID.randomUUID()));

        Song song = songRepository.save(new Song(album, "Test Song Title", 210L, "https://license.test", 1L, "https://audio.test/" + UUID.randomUUID() + ".mp3", "VOCAL", "en", "120", "guitar, drums", "pop, test"));

        int num = 10;

        // 여러명이 동시에 접근할 수 있도록 설정
        ExecutorService executor = Executors.newFixedThreadPool(num);

        Runnable task = () -> {

            try {
                songService.play(song.getSongId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        for (int i = 0; i < num; i++) {
            executor.submit(task);
        }

        executor.shutdown();

        // 실행 환경이 충분히 실행완료 될때까지
        Thread.sleep(5000);

        Song result = songRepository.findById(song.getSongId()).orElseThrow();

        assertThat(result.getPlaycount()).isEqualTo(num);

        System.out.println("최종 결과 : " + result.getPlaycount());

    }
}
