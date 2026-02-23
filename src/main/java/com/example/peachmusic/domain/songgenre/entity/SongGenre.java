package com.example.peachmusic.domain.songgenre.entity;

import com.example.peachmusic.domain.genre.entity.Genre;
import com.example.peachmusic.domain.song.entity.Song;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "song_genres")
public class SongGenre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "song_genre_id")
    private Long songGenreId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "genre_id", nullable = false)
    private Genre genre;

    public SongGenre(Song song, Genre genre) {
        this.song = song;
        this.genre = genre;
    }
}
