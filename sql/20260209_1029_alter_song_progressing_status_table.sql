ALTER TABLE song_progressing_status
    CHANGE COLUMN streaming_job_id song_progressing_status_id BIGINT NOT NULL AUTO_INCREMENT,
    CHANGE COLUMN job_status progressing_status VARCHAR (32) NOT NULL