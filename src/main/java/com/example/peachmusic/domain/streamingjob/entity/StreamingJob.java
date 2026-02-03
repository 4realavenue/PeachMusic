package com.example.peachmusic.domain.streamingjob.entity;

import com.example.peachmusic.common.enums.JobStatus;
import com.example.peachmusic.domain.song.entity.Song;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "streaming_jobs")
public class StreamingJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "streaming_job_id")
    private Long streamingJobId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "job_status", nullable = false)
    private JobStatus jobStatus;

    public StreamingJob(Song song, JobStatus jobStatus) {
        this.song = song;
        this.jobStatus = jobStatus;
    }
}
