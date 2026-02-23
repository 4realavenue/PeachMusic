package com.example.peachmusic.domain.playlist.entity;

import com.example.peachmusic.common.model.BaseEntity;
import com.example.peachmusic.domain.playlist.dto.request.PlaylistUpdateRequestDto;
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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "playlist_name", nullable = false)
    private String playlistName;

    @Column(name = "playlist_image")
    private String playlistImage;

    public Playlist(User user, String playlistName, String playlistImage) {
        this.user = user;
        this.playlistName = playlistName;
        this.playlistImage = playlistImage;
    }

    public void updatePlaylistName(PlaylistUpdateRequestDto requestDto) {
        this.playlistName = (requestDto.getPlaylistName() == null || requestDto.getPlaylistName().isBlank()) ? this.playlistName : requestDto.getPlaylistName();
    }

    public void updatePlaylistImage(String playlistImage) {
        this.playlistImage = playlistImage;
    }

    public boolean isOwnedBy(Long id) {
        return this.user.getUserId().equals(id);
    }
}
