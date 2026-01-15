package com.example.peachmusic.domain.playlist.entity;

import com.example.peachmusic.common.entity.BaseEntity;
import com.example.peachmusic.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "playlists")
public class Playlist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "playlist_id")
    private Long playlistId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Column(name = "user_id")
    private User user;

    @Column(name = "playlist_name", nullable = false)
    private String playlistName;

    public Playlist(User user, String playlistName) {
        this.user = user;
        this.playlistName = playlistName;
    }
}
