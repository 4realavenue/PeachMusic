package com.example.peachmusic.domain.song.entity;

import com.example.peachmusic.common.model.BaseEntity;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.song.dto.request.AdminSongUpdateRequestDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "songs")
public class Song extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "song_id")
    private Long songId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @Column(name = "jamendo_song_id", unique = true)
    private Long jamendoSongId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "duration", nullable = false)
    private Long duration;

    @Column(name = "license_ccurl")
    private String licenseCcurl;

    @Column(name = "position", nullable = false)
    private Long position;

    @Column(name = "audio", nullable = false, unique = true)
    private String audio;

    @Column(name = "vocalinstrumental")
    private String vocalinstrumental;

    @Column(name = "lang")
    private String lang;

    @Column(name = "speed")
    private String speed;

    @Column(name = "instruments")
    private String instruments;

    @Column(name = "vartags")
    private String vartags;

    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @Column(name = "streaming_status")
    private boolean streamingStatus = false; // 기본값 = 스트리밍 불가능

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "play_count", nullable = false)
    private Long playCount = 0L;

    public Song(Album album, String name, Long duration, String licenseCcurl, Long position, String audio, String vocalinstrumental, String lang, String speed, String instruments, String vartags) {
        this.album = album;
        this.name = name;
        this.duration = duration;
        this.licenseCcurl = licenseCcurl;
        this.position = position;
        this.audio = audio;
        this.vocalinstrumental = vocalinstrumental;
        this.lang = lang;
        this.speed = speed;
        this.instruments = instruments;
        this.vartags = vartags;
    }

    public Song(Long jamendoSongId, Album album, String name, Long duration, String licenseCcurl, Long position, String audio, String vocalinstrumental, String lang, String speed, String instruments, String vartags) {
        this.jamendoSongId = jamendoSongId;
        this.album = album;
        this.name = name;
        this.duration = duration;
        this.licenseCcurl = licenseCcurl;
        this.position = position;
        this.audio = audio;
        this.vocalinstrumental = vocalinstrumental;
        this.lang = lang;
        this.speed = speed;
        this.instruments = instruments;
        this.vartags = vartags;
    }

    public void updateSong(AdminSongUpdateRequestDto requestDto, Long newPosition, Album album) {
        this.name = (requestDto.getName() == null || requestDto.getName().isBlank()) ? this.name : requestDto.getName();
        this.duration = (requestDto.getDuration() == null) ? this.duration : requestDto.getDuration();
        this.licenseCcurl = (requestDto.getLicenseCcurl() == null || requestDto.getLicenseCcurl().isBlank()) ? this.getLicenseCcurl() : requestDto.getLicenseCcurl();
        this.position = (newPosition == null) ? this.position : newPosition;
        this.vocalinstrumental = (requestDto.getVocalinstrumental() == null || requestDto.getVocalinstrumental().isBlank()) ? this.getVocalinstrumental() : requestDto.getVocalinstrumental();
        this.lang = (requestDto.getLang() == null || requestDto.getLang().isBlank()) ? this.lang : requestDto.getLang();
        this.speed = (requestDto.getSpeed() == null || requestDto.getSpeed().isBlank()) ? this.speed : requestDto.getSpeed();
        this.instruments = (requestDto.getInstruments() == null || requestDto.getInstruments().isBlank()) ? this.instruments : requestDto.getInstruments();
        this.vartags = (requestDto.getVartags() == null || requestDto.getVartags().isBlank()) ? this.vartags : requestDto.getVartags();
        this.album = album;
    }

    public void deleteSong() {
        this.isDeleted = true;
    }

    public void restoreSong() {
        this.isDeleted = false;
    }

    public void updateAudio(String audio) {
        this.audio = audio;
    }

    public void addPlayCount() {
        this.playCount++;
    }
}
