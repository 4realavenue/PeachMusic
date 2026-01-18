package com.example.peachmusic.domain.song.entity;

import com.example.peachmusic.common.entity.BaseEntity;
import com.example.peachmusic.domain.album.entity.Album;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

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

    public void updateSong(String name, Long duration, String licenseCcurl, Long position, String audio, String vocalinstrumental, String lang, String speed, String instruments, String vartags, Album album) {
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
        this.album = album;
    }

    public void deleteSong() {
        this.isDeleted = true;
    }

    public void restoreSong() {
        this.isDeleted = false;
    }

    public void unlikeSong() {
        if(this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void likeSong() {
        this.likeCount++;
    }

}
