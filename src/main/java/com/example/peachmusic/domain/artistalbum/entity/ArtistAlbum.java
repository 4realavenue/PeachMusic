package com.example.peachmusic.domain.artistalbum.entity;

import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.artist.entity.Artist;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "artist_albums")
public class ArtistAlbum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "artist_album_id")
    private Long artistAlbumId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    public ArtistAlbum(Artist artist, Album album) {
        this.artist = artist;
        this.album = album;
    }
}
