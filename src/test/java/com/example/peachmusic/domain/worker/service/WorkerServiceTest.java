package com.example.peachmusic.domain.worker.service;

import com.example.peachmusic.domain.worker.dto.request.WorkerTryRequestDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class WorkerServiceTest {

    private MockWebServer mockWebServer;
    private WorkerService workerService;

    private final ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();

        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        workerService = new WorkerService(webClient);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void tryDownloadSong_정상요청_POST_경로와바디검증() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        WorkerTryRequestDto dto = buildDto();

        workerService.tryDownloadSong(dto);

        RecordedRequest req = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
        assertThat(req).isNotNull();
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getPath()).isEqualTo("/worker/songs/download-request");
        assertThat(req.getHeader(HttpHeaders.CONTENT_TYPE)).contains(MediaType.APPLICATION_JSON_VALUE);

        JsonNode expected = om.readTree(om.writeValueAsString(dto));
        JsonNode actual = om.readTree(req.getBody().readUtf8());
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void tryTranscodeSong_정상요청_POST_경로와바디검증() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        WorkerTryRequestDto dto = buildDto();

        workerService.tryTranscodeSong(dto);

        RecordedRequest req = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
        assertThat(req).isNotNull();
        assertThat(req.getMethod()).isEqualTo("POST");
        assertThat(req.getPath()).isEqualTo("/worker/songs/transcode-request");
        assertThat(req.getHeader(HttpHeaders.CONTENT_TYPE)).contains(MediaType.APPLICATION_JSON_VALUE);

        JsonNode expected = om.readTree(om.writeValueAsString(dto));
        JsonNode actual = om.readTree(req.getBody().readUtf8());
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void worker가_끊기면_doOnError_로그가찍힌다(CapturedOutput output) throws Exception {
        mockWebServer.enqueue(
                new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
        );

        WorkerTryRequestDto dto = buildDto();

        workerService.tryDownloadSong(dto);

        boolean logged = waitUntil(() ->
                        output.getOut().contains("Worker 불러오기 실패")
                                || output.getErr().contains("Worker 불러오기 실패"),
                1500
        );

        assertThat(logged).isTrue();
    }

    private WorkerTryRequestDto buildDto() {
         return new WorkerTryRequestDto(List.of(1L, 2L, 3L));
    }

    private boolean waitUntil(Check cond, long timeoutMs) throws InterruptedException {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            if (cond.ok()) return true;
            Thread.sleep(50);
        }
        return false;
    }

    @FunctionalInterface
    interface Check {
        boolean ok();
    }
}
