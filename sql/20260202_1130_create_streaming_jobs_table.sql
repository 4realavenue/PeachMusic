CREATE TABLE streaming_jobs
(
    streaming_job_id BIGINT      NOT NULL AUTO_INCREMENT COMMENT '음원 변환 작업 상태 고유 식별자',
    song_id          BIGINT      NOT NULL COMMENT '음원 고유 식별자',
    job_status       VARCHAR(32) NOT NULL COMMENT '음원 변환 작업 상태',

    PRIMARY KEY (streaming_job_id),

    UNIQUE KEY uk_streaming_jobs_song_id (song_id),

    KEY idx_streaming_jobs_song_id (song_id),

    CONSTRAINT fk_streaming_jobs_song_id FOREIGN KEY (song_id) REFERENCES songs (song_id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;