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

    public Artist(String artistName) {
        this.artistName = artistName;
    }

    public void updateArtistName(String artistName) {
        if (artistName != null && !artistName.isBlank()) {
            this.artistName = artistName.trim();
        }
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

    public boolean isSameName(String newName) {
        if (newName == null || newName.isBlank()) {
            return true;
        }
        return this.artistName.equals(newName.trim());
    }
}
