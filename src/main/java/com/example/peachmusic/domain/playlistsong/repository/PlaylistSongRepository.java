package com.example.peachmusic.domain.playlistsong.repository;

import com.example.peachmusic.domain.playlist.entity.Playlist;
import com.example.peachmusic.domain.playlistsong.entity.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {

    List<PlaylistSong> findAllByPlaylist(Playlist playlist);

    void deleteAllByPlaylist(Playlist playlist);

    @Query("""
            select ps.song.songId from PlaylistSong ps
            where ps.playlist.playlistId = :playlistId and ps.song.songId in (:songIdSet)
            """)
    Set<Long> findSongIdSetByPlaylist_PlaylistIdAndSong_SongIdSet(Long playlistId, Set<Long> songIdSet);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from PlaylistSong ps
            where ps.playlist.playlistId = :playlistId and ps.song.songId in (:songList)
            """)
    void deletePlaylistSongByPlaylist_PlaylistIdAndSong_SongId(Long playlistId, List<Long> songList);


    @Query("""
            select ps.song.songId
              from PlaylistSong ps
             where ps.playlist.user.userId = :userId
            """)
    List<Long> findSongsPlaylistByUser(Long userId);
}
