package com.example.peachmusic.domain.playlistSong.repository;

import com.example.peachmusic.domain.playlist.entity.Playlist;
import com.example.peachmusic.domain.playlistSong.entity.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {

    boolean existsByPlaylist_PlaylistIdAndSong_SongId(Long playlistId, Long songId);

    List<PlaylistSong> findAllByPlaylist(Playlist playlist);

    void deleteAllByPlaylist(Playlist playlist);

    @Query("""
            select ps.song.songId from PlaylistSong ps
            where ps.playlist.playlistId = :playlistId and ps.song.songId in (:songIdList)
            """)
    List<Long> findSongIdListByPlaylist_PlaylistIdAndSong_SongId(Long playlistId, List<Long> songIdList);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from PlaylistSong ps
            where ps.playlist.playlistId = :playlistId and ps.song.songId in (:songList)
            """)
    void deletePlaylistSongByPlaylist_PlaylistIdAndSong_SongId(Long playlistId, List<Long> songList);


}
