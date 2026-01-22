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

    @Column(name = "jamendo_album_id", unique = true)
    private Long jamendoAlbumId;

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

    public Album(Long jamendoAlbumId, String albumName, LocalDate albumReleaseDate, String albumImage) {
        this.jamendoAlbumId = jamendoAlbumId;
        this.albumName = albumName;
        this.albumReleaseDate = albumReleaseDate;
        this.albumImage = albumImage;
    }

    public void updateAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public void updateAlbumReleaseDate(LocalDate albumReleaseDate) {
        this.albumReleaseDate = albumReleaseDate;
    }

    public void updateAlbumImage(String albumImage) {
        this.albumImage = albumImage;
    }

    public void delete() {
        this.isDeleted = true;
    }

    public void restore() {
        this.isDeleted = false;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}
