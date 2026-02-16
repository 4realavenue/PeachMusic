package com.example.peachmusic.domain.search.service;

import com.example.peachmusic.domain.search.dto.SearchPopularResponseDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisServer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SearchHistoryServiceTest {

    private static final GenericContainer<?> redis;

    static {
        redis = new GenericContainer<>("redis:7.2").withExposedPorts(6379);
        redis.start();
    }

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private SearchHistoryService searchHistoryService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private RedisServer redisServer;

    private static final String SEARCH_KEY = "searchWord:";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH");

    @BeforeEach
    void clearRedis() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    /**
     * 최근 24시간 집계 정확성 검증
     */
    @Test
    void 최근24시간_인기검색어_집계_정확성_테스트() {

        // given
        LocalDateTime now = LocalDateTime.now();

        String keyNow = SEARCH_KEY + now.format(FORMATTER); // 현재 시간
        redisTemplate.opsForZSet().add(keyNow, "아이유", 1);
        redisTemplate.opsForZSet().add(keyNow, "로이킴", 1);

        String key11HourAgo = SEARCH_KEY + now.minusHours(23).format(FORMATTER); // 23시간 전
        redisTemplate.opsForZSet().add(key11HourAgo, "아이유", 1);

        String key12HourAgo = SEARCH_KEY + now.minusHours(24).format(FORMATTER); // 24시간 전
        redisTemplate.opsForZSet().add(key12HourAgo, "로이킴", 1);

        // when
        List<SearchPopularResponseDto> result = searchHistoryService.searchPopular();

        // then
        assertThat(result).isNotEmpty();

        assertThat(result)
                .extracting(SearchPopularResponseDto::getKeyword, SearchPopularResponseDto::getCount)
                .containsExactlyInAnyOrder(tuple("아이유", 2L), tuple("로이킴", 1L));

        assertThat(result.stream()
                .filter(r -> r.getKeyword().equals("아이유"))
                .findFirst()
                .get()
                .getRank()).isEqualTo(1);
    }
}

