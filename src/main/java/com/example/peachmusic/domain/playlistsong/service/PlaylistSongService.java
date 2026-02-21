package com.example.peachmusic.domain.playlistsong.service;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.artistsong.repository.ArtistSongRepository;
import com.example.peachmusic.domain.playlist.dto.response.PlaylistGetSongResponseDto;
import com.example.peachmusic.domain.playlist.entity.Playlist;
import com.example.peachmusic.domain.playlist.repository.PlaylistRepository;
import com.example.peachmusic.domain.playlistsong.dto.request.PlaylistSongAddRequestDto;
import com.example.peachmusic.domain.playlistsong.dto.request.PlaylistSongDeleteRequestDto;
import com.example.peachmusic.domain.playlistsong.dto.response.PlaylistSongAddResponseDto;
import com.example.peachmusic.domain.playlistsong.dto.response.PlaylistSongDeleteSongResponseDto;
import com.example.peachmusic.domain.playlistsong.entity.PlaylistSong;
import com.example.peachmusic.domain.playlistsong.repository.PlaylistSongRepository;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.repository.SongRepository;
import com.example.peachmusic.domain.songlike.repository.SongLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaylistSongService {


    private final PlaylistSongRepository playlistSongRepository;
    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;
    private final SongLikeRepository songLikeRepository;
    private final ArtistSongRepository artistSongRepository;

    /**
     * 플레이리스트 음원 조회
     */
    @Transactional(readOnly = true)
    public PlaylistGetSongResponseDto getPlaylistSongList(Long playlistId, AuthUser authUser) {

        Long userId = authUser.getUserId();

        Playlist findPlaylist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAYLIST_NOT_FOUND));

        if (!findPlaylist.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.AUTH_AUTHORIZATION_REQUIRED);
        }

        List<PlaylistSong> findPlaylistSongList = playlistSongRepository.findAllByPlaylist(findPlaylist);
        List<Long> findSongIdList = findPlaylistSongList.stream().map(playlistSong -> playlistSong.getSong().getSongId()).toList();

        Map<Long, String> artistNameMapBySongId = artistSongRepository.findAllBySongIdList(findSongIdList).stream()
                .collect(Collectors.groupingBy(artistSong -> artistSong.getSong().getSongId(),
                        Collectors.mapping(artistSong -> artistSong.getArtist().getArtistName(), Collectors.joining(", "))));

        Set<Long> likedSongIdSet = songLikeRepository.findLikedSongIdList(userId, findSongIdList);

        List<PlaylistGetSongResponseDto.SongResponseDto> songResponseDtoList = findPlaylistSongList.stream()
                .map(playlistSong -> {
                    Song song = playlistSong.getSong();

                    Album album = playlistSong.getSong().getAlbum();

                    String artistName = artistNameMapBySongId.getOrDefault(song.getSongId(), "");
                    boolean isLiked = likedSongIdSet.contains(song.getSongId());

                    return PlaylistGetSongResponseDto.SongResponseDto.from(playlistSong, album, artistName, isLiked);
                }).toList();

        return PlaylistGetSongResponseDto.from(findPlaylist, songResponseDtoList);
    }

    /**
     * 플레이리스트 음원 추가
     */
    @Transactional
    public PlaylistSongAddResponseDto addPlaylistSong(Long playlistId, PlaylistSongAddRequestDto requestDto, AuthUser authUser) {

        Long userId = authUser.getUserId();

        Playlist findPlaylist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAYLIST_NOT_FOUND));

        if (!findPlaylist.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.AUTH_AUTHORIZATION_REQUIRED);
        }

        if (requestDto.getSongIdSet() == null || requestDto.getSongIdSet().isEmpty()) {
            throw new CustomException(ErrorCode.PLAYLIST_ADD_SONG_REQUIRED);
        }

        Set<Long> validRequestSongIdSet = songRepository.findSongIdSetBySongIdSet(requestDto.getSongIdSet());

        Set<Long> duplicationSongIdSet = playlistSongRepository.findSongIdSetByPlaylist_PlaylistIdAndSong_SongIdSet(findPlaylist.getPlaylistId(), validRequestSongIdSet);

        List<Long> validSongIdList = validRequestSongIdSet.stream()
                .filter(songId -> !duplicationSongIdSet.contains(songId)).toList();

        List<PlaylistSong> playlistSongList = validSongIdList.stream()
                .map(songRepository::getReferenceById).map(song -> new PlaylistSong(song, findPlaylist)).toList();

        playlistSongRepository.saveAll(playlistSongList);

        return PlaylistSongAddResponseDto.from(findPlaylist.getPlaylistId(), validSongIdList, validSongIdList.size());

    }

    /**
     * 플레이리스트의 음원 삭제
     */
    @Transactional
    public PlaylistSongDeleteSongResponseDto deletePlaylistSong(Long playlistId, PlaylistSongDeleteRequestDto requestDto, AuthUser authUser) {

        Long userId = authUser.getUserId();

        Playlist findPlaylist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLAYLIST_NOT_FOUND));

        if (!findPlaylist.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.AUTH_AUTHORIZATION_REQUIRED);
        }

        if (requestDto.getSongIdSet() == null || requestDto.getSongIdSet().isEmpty()) {
            throw new CustomException(ErrorCode.PLAYLIST_REMOVE_SONG_REQUIRED);
        }

        Set<Long> validRequestSongIdSet = songRepository.findSongIdSetBySongIdSet(requestDto.getSongIdSet());

        Set<Long> duplicationSongIdSet = playlistSongRepository.findSongIdSetByPlaylist_PlaylistIdAndSong_SongIdSet(findPlaylist.getPlaylistId(), validRequestSongIdSet);

        List<Long> validSongIdList = validRequestSongIdSet.stream()
                .filter(duplicationSongIdSet::contains).toList();

        playlistSongRepository.deletePlaylistSongByPlaylist_PlaylistIdAndSong_SongId(findPlaylist.getPlaylistId(), validSongIdList);

        return PlaylistSongDeleteSongResponseDto.from(findPlaylist, validSongIdList);

    }
}
