package com.example.peachmusic.domain.playlistSong.entity;

import com.example.peachmusic.domain.playlist.entity.Playlist;
import com.example.peachmusic.domain.song.entity.Song;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "playlist_songs")
public class PlaylistSong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "playlist_song_id")
    private Long playlistSongId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Column(name = "song_id")
    private Song song;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Column(name = "playlist_id")
    private Playlist playlist;

    public PlaylistSong(Song song, Playlist playlist) {
        this.song = song;
        this.playlist = playlist;
    }
}
