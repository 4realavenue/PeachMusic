package com.example.peachmusic.domain.songlike.service;

import com.example.peachmusic.common.enums.UserRole;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.repository.SongRepository;
import com.example.peachmusic.domain.songlike.repository.SongLikeRepository;
import com.example.peachmusic.domain.user.entity.User;
import com.example.peachmusic.domain.user.repository.UserRepository;
import com.example.peachmusic.domain.user.service.UserService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(SongLikeTxServiceTest.TestBeans.class)
class SongLikeTxServiceTest {

    private static final Logger log = LoggerFactory.getLogger(SongLikeTxServiceTest.class);

    @Autowired
    SongRepository songRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AlbumRepository albumRepository;

    @Autowired
    private SongLikeTxService songLikeTxService;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Autowired
    EntityManager em;

    @TestConfiguration
    static class TestBeans {
        @Bean
        UserService userService(UserRepository userRepository) {
            return new UserService(userRepository);
        }

        @Bean
        SongLikeTxService songLikeTxService(SongLikeRepository songLikeRepository, SongRepository songRepository) {
            return new SongLikeTxService(songLikeRepository, songRepository);
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void likeSong_concurrency_with_multiple_users_test() throws Exception {

        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        List<Long> userIdList = tx.execute(status -> {
            List<Long> idList = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                User user = userRepository.save(new User("test-name" + i, "test-nickname" + i, "test" + i +"@test.com", "test1234!"));
                idList.add(user.getUserId());
            }
            userRepository.flush();
            return idList;
        });

        Long albumId = tx.execute(status -> {
            Album album = albumRepository.save(new Album(
                    "테스트 앨범",
                    LocalDate.of(2024, 1, 1),
                    "https://image.test/" + UUID.randomUUID()
            ));
            albumRepository.flush();
            return album.getAlbumId();
        });

        Long songId = tx.execute(status -> {
            Album managedAlbum = albumRepository.findByAlbumIdAndIsDeletedFalse(albumId)
                    .orElseThrow();

            Song song = songRepository.save(new Song(
                    managedAlbum,
                    "Test Song Title",
                    210L,
                    "https://license.test",
                    1L,
                    "https://audio.test/" + UUID.randomUUID() + ".mp3",
                    "VOCAL",
                    "en",
                    "120",
                    "guitar, drums",
                    "pop, test"
            ));

            songRepository.flush();
            return song.getSongId();
        });

        int threadCount = userIdList.size();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        ConcurrentHashMap<String, AtomicInteger> errMap = new ConcurrentHashMap<>();

        for (Long userId : userIdList) {
            executor.submit(() -> {
                try {
                    AuthUser authUser = new AuthUser(userId, "test" + userId + "@test.com", UserRole.USER, 0L);
                    retryOnLock(() -> songLikeTxService.doLikeSong(authUser, songId));

                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    errMap.computeIfAbsent(e.getClass().getSimpleName(), k -> new AtomicInteger()).incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        Long likeCount = songRepository.findLikeCountBySongId(songId);

        Long pairCount = em.createQuery("""
             select count(sl) from SongLike sl
             where sl.song.songId = :songId
        """, Long.class)
                .setParameter("songId", songId)
                .getSingleResult();

        int total = threadCount;
        int success = successCount.get();
        int fail = errorCount.get();
        double successRate = (success * 100.0) / total;

        errMap.forEach((k,v) -> log.info("err {} = {}", k, v.get()));

        log.info("Song total = {}", total);
        log.info("Song success = {}", success);
        log.info("Song fail = {}", fail);
        log.info("Song successRate = {}%", String.format("%.2f", successRate));
        log.info("Song final likeCount = {}", likeCount);
        log.info("Song pairCount = {}", pairCount);

        assertThat(likeCount).isEqualTo(pairCount);
    }

    private void retryOnLock(Runnable action) {
        int maxRetry = 3;
        for (int i = 0; i < maxRetry; i++) {
            try {
                action.run();
                return;
            } catch (org.springframework.dao.CannotAcquireLockException e) {
                if (i == maxRetry - 1) {
                    throw e;
                }
                try { Thread.sleep(10L + ThreadLocalRandom.current().nextLong(20)); }
                catch (InterruptedException ignored) {}
            }
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void likeSong_duplicate_request_by_same_user_test() throws InterruptedException {

        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        Long userId = tx.execute(status -> {
            User user = userRepository.save(new User("test-name", "test-nickname", "test@test.com", "test1234!"));
            userRepository.flush();
            return user.getUserId();
        });

        Long albumId = tx.execute(status -> {
            Album album = albumRepository.save(new Album(
                    "테스트 앨범",
                    LocalDate.of(2024, 1, 1),
                    "https://image.test/" + UUID.randomUUID()
            ));
            albumRepository.flush();
            return album.getAlbumId();
        });

        Long songId = tx.execute(status -> {
            Album managedAlbum = albumRepository.findByAlbumIdAndIsDeletedFalse(albumId)
                    .orElseThrow();

            Song song = songRepository.save(new Song(
                    managedAlbum,
                    "Test Song Title",
                    210L,
                    "https://license.test",
                    1L,
                    "https://audio.test/" + UUID.randomUUID() + ".mp3",
                    "VOCAL",
                    "en",
                    "120",
                    "guitar, drums",
                    "pop, test"
            ));

            songRepository.flush();
            return song.getSongId();
        });

        AuthUser authUser = new AuthUser(userId, "test@test.com", UserRole.USER, 0L);

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        ConcurrentHashMap<String, AtomicInteger> errMap = new ConcurrentHashMap<>();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    songLikeTxService.doLikeSong(authUser, songId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    errMap.computeIfAbsent(e.getClass().getSimpleName(), k -> new AtomicInteger()).incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        Long likeCount = songRepository.findLikeCountBySongId(songId);
        Long pairCount = em.createQuery("""
             select count(sl) from SongLike sl
             where sl.song.songId = :songId
             and sl.user.userId = :userId
        """, Long.class)
                .setParameter("songId", songId)
                .setParameter("userId", userId)
                .getSingleResult();

        int total = threadCount;
        int success = successCount.get();
        int fail = errorCount.get();

        errMap.forEach((k,v) -> log.info("err {} = {}", k, v.get()));

        log.info("Song total = {}", total);
        log.info("Song success = {}", success);
        log.info("Song fail = {}", fail);
        log.info("Song final likeCount = {}", likeCount);
        log.info("Song pairCount = {}", pairCount);

        assertThat(pairCount).isBetween(0L, 1L);
        assertThat(likeCount).isEqualTo(pairCount);
    }
}