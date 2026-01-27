package com.example.peachmusic.domain.artistsong.entity;

import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.song.entity.Song;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "artist_songs")
public class ArtistSong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "artist_song_id")
    private Long artistSongId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    public ArtistSong(Artist artist, Song song) {
        this.artist = artist;
        this.song = song;
    }
}
