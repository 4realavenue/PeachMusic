package com.example.peachmusic.domain.artistsong.repository;

import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artistsong.entity.ArtistSong;
import com.example.peachmusic.domain.song.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ArtistSongRepository extends JpaRepository<ArtistSong, Long> {

    @Query("""
SELECT ars.artist FROM ArtistSong ars
WHERE ars.song = :song
""")
    List<Artist> findArtistListBySong(Song song);
}
