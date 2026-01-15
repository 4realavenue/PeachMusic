package com.example.peachmusic.domain.artistLike.entity;

import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "artist_likes")
public class ArtistLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "artist_like_id")
    private Long artistLikeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Column(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Column(name = "artist_id")
    private Artist artist;

    public ArtistLike(User user, Artist artist) {
        this.user = user;
        this.artist = artist;
    }
}
