package com.example.peachmusic.domain.album.entity;

import com.example.peachmusic.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table (name = "albums")
public class Album extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "album_id")
    private Long albumId;

    @Column(name = "album_name", nullable = false)
    private String albumName;

    @Column(name = "album_release_date", nullable = false)
    private LocalDate albumReleaseDate;

    @Column(name = "album_image", nullable = false, unique = true)
    private String albumImage;

    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    public Album(String albumName, LocalDate albumReleaseDate, String albumImage) {
        this.albumName = albumName;
        this.albumReleaseDate = albumReleaseDate;
        this.albumImage = albumImage;
    }
}
