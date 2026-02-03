package com.example.peachmusic.domain.albumlike.service;

import com.example.peachmusic.common.enums.UserRole;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.albumlike.repository.AlbumLikeRepository;
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

/**
 * 좋아요 동시성 상황에서 insertIgnore 방식의 안정성을 검증하는 테스트
 * - 서로 다른 유저 100명이 동시에 좋아요 요청
 * - 단일 유저의 중복 좋아요 요청
 * - likeCount와 실제 row 수 정합성 확인
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AlbumLikeTxServiceTest.TestBeans.class)
class AlbumLikeTxServiceTest {

    private static final Logger log = LoggerFactory.getLogger(AlbumLikeTxServiceTest.class);

    @Autowired
    AlbumRepository albumRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AlbumLikeTxService albumLikeTxService;

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
        AlbumLikeTxService albumLikeTxService(AlbumLikeRepository albumLikeRepository, AlbumRepository albumRepository) {
            return new AlbumLikeTxService(albumLikeRepository, albumRepository);
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void likeAlbum_concurrency_with_multiple_users_test() throws Exception {

        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        // 유저 100명 생성
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

        int threadCount = userIdList.size();

        // 동시에 좋아요 요청을 보내 동시성 충돌 유도
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        ConcurrentHashMap<String, AtomicInteger> errMap = new ConcurrentHashMap<>();

        // 동시 좋아요 실행
        for (Long userId : userIdList) {
            executor.submit(() -> {
                try {
                    AuthUser authUser = new AuthUser(userId, "test" + userId + "@test.com", UserRole.USER, 0L);
                    retryOnLock(() -> albumLikeTxService.doLikeAlbum(authUser, albumId));

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

        // 결과 조회
        Long likeCount = albumRepository.findLikeCountByAlbumId(albumId);

        // 전체 좋아요 row 수
        Long pairCount = em.createQuery("""
             select count(al) from AlbumLike al
             where al.album.albumId = :albumId
        """, Long.class)
                .setParameter("albumId", albumId)
                .getSingleResult();

        int total = threadCount;
        int success = successCount.get();
        int fail = errorCount.get();
        double successRate = (success * 100.0) / total;

        errMap.forEach((k,v) -> log.info("err {} = {}", k, v.get()));

        log.info("Album total = {}", total); // 총 좋아요 수
        log.info("Album success = {}", success); // 성공한 요청 수
        log.info("Album fail = {}", fail); // 실패한 요청 수
        log.info("Album successRate = {}%", String.format("%.2f", successRate)); // 성공률
        log.info("Album final likeCount = {}", likeCount); // 저장된 좋아요 수
        log.info("Album pairCount = {}", pairCount); // 실제 좋아요 row 수

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
    void likeAlbum_duplicate_request_by_same_user_test() throws InterruptedException {

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

        AuthUser authUser = new AuthUser(userId, "test@test.com", UserRole.USER, 0L);

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(threadCount);
        ConcurrentHashMap<String, AtomicInteger> errMap = new ConcurrentHashMap<>();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    albumLikeTxService.doLikeAlbum(authUser, albumId);
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

        Long likeCount = albumRepository.findLikeCountByAlbumId(albumId);

        // 특정 유저-앨범 조합 row 수
        Long pairCount = em.createQuery("""
             select count(al) from AlbumLike al
             where al.album.albumId = :albumId
             and al.user.userId = :userId
        """, Long.class)
                .setParameter("albumId", albumId)
                .setParameter("userId", userId)
                .getSingleResult();

        int total = threadCount;
        int success = successCount.get();
        int fail = errorCount.get();

        errMap.forEach((k,v) -> log.info("err {} = {}", k, v.get()));

        log.info("Album total = {}", total);
        log.info("Album success = {}", success);
        log.info("Album fail = {}", fail);
        log.info("Album final likeCount = {}", likeCount);
        log.info("Album pairCount = {}", pairCount);

        // 토글 API 특성상 동시 요청 시 최종 상태는 0 또는 1이 될 수 있음
        // 현재 구현에서는 테스트 결과가 대부분 1로 나오지만,
        // 중요한 것은 중복 row가 생성되지 않는지와 likeCount 정합성임
        assertThat(pairCount).isBetween(0L, 1L);
        assertThat(likeCount).isEqualTo(pairCount);
    }
}