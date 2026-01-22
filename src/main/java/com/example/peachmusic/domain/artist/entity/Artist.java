package com.example.peachmusic.domain.artist.entity;

import com.example.peachmusic.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table (name = "artists")
public class Artist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "artist_id")
    private Long artistId;

    @Column(name = "artist_name", nullable = false, unique = true)
    private String artistName;

    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "jamendo_artist_id", unique = true)
    private Long jamendoArtistId;

    public Artist(String artistName) {
        this.artistName = artistName;
    }

    public Artist(String artistName, Long jamendoArtistId) {
        this.artistName = artistName;
        this.jamendoArtistId = jamendoArtistId;
    }

    public void updateArtistName(String artistName) {
        this.artistName = artistName;
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
