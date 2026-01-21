package com.example.peachmusic.domain.albumLike.entity;

import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table (name = "album_likes")
public class AlbumLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "album_like_id")
    private Long albumLikeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    public AlbumLike(User user, Album album) {
        this.user = user;
        this.album = album;
    }
}
