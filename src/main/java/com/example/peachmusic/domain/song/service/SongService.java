package com.example.peachmusic.domain.song.service;

import com.example.peachmusic.common.annotation.RedisLock;
import com.example.peachmusic.common.constants.RedisResetTime;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.model.*;
import com.example.peachmusic.domain.album.dto.response.ArtistSummaryDto;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.artistsong.repository.ArtistSongRepository;
import com.example.peachmusic.domain.song.dto.response.SongArtistDetailResponseDto;
import com.example.peachmusic.domain.song.dto.response.SongGetDetailResponseDto;
import com.example.peachmusic.domain.song.dto.response.SongPlayResponseDto;
import com.example.peachmusic.domain.song.dto.response.SongSearchResponseDto;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.repository.SongRepository;
import com.example.peachmusic.domain.songgenre.entity.SongGenre;
import com.example.peachmusic.domain.songgenre.repository.SongGenreRepository;
import com.example.peachmusic.domain.songlike.repository.SongLikeRepository;
import com.example.peachmusic.domain.user.entity.User;
import com.example.peachmusic.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static com.example.peachmusic.common.constants.SearchViewSize.DETAIL_SIZE;
import static com.example.peachmusic.common.constants.SearchViewSize.PREVIEW_SIZE;
import static com.example.peachmusic.common.constants.UserViewScope.PUBLIC_VIEW;
import static com.example.peachmusic.common.enums.SortDirection.DESC;
import static com.example.peachmusic.common.enums.SortType.LIKE;

@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;
    private final SongGenreRepository songGenreRepository;
    private final AlbumRepository albumRepository;
    private final SongLikeRepository songLikeRepository;
    private final ArtistRepository artistRepository;
    private final UserService userService;
    private final ArtistSongRepository artistSongRepository;

    private final RedisTemplate<String,String> redisTemplate;

    public static final String MUSIC_DAILY_KEY = "music";

    @Value("${r2.public-streaming-base}")
    private String streamingBaseUrl;

    /**
     * 음원 전체 조회
     */
    @Transactional(readOnly = true)
    public KeysetResponse<SongSearchResponseDto> getSongList(AuthUser authUser, SortType sortType, SortDirection direction, CursorParam cursor) {

        List<SongSearchResponseDto> content = songRepository.findSongKeysetPageByWord(authUser, null, DETAIL_SIZE, PUBLIC_VIEW, sortType, direction, cursor);

        return KeysetResponse.of(content, DETAIL_SIZE, last -> last.toCursor(sortType));
    }

    /**
     * 음원 단건 조회
     */
    @Transactional(readOnly = true)
    public SongGetDetailResponseDto getSong(Long songId, AuthUser authUser) {

        Song findSong = songRepository.findBySongIdAndIsDeletedFalseAndStreamingStatusTrue(songId)
                .orElseThrow(() -> new CustomException(ErrorCode.SONG_NOT_FOUND));

        boolean liked = false;

        if (authUser != null) { // 로그인이 된 경우
            User findUser = userService.findUser(authUser);
            if (songLikeRepository.existsSongLikeByUserAndSong(findUser, findSong)) {
                liked = true;
            }
        }

        Long findAlbumId = songRepository.findSongs_AlbumIdBySongId(findSong);

        Album findAlbum = albumRepository.findActiveAlbumWithActiveSong(findAlbumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        List<SongGenre> findSongGenreList = songGenreRepository.findAllBySong(findSong);

        List<String> genreNameList = findSongGenreList.stream()
                .map(songGenre -> songGenre.getGenre().getGenreName())
                .toList();

        List<Artist> findArtist = artistSongRepository.findArtistListBySong(findSong);

        List<ArtistSummaryDto> artistList = findArtist.stream()
                .map(ArtistSummaryDto::from)
                .toList();

        return SongGetDetailResponseDto.from(findAlbum, artistList, findSong, genreNameList, liked);

    }

    /**
     * 아티스트의 음원 자세히 보기
     */
    @Transactional(readOnly = true)
    public KeysetResponse<SongArtistDetailResponseDto> getArtistSongs(AuthUser authUser, Long artistId, CursorParam cursor) {

        Artist foundArtist = artistRepository.findByArtistIdAndIsDeletedFalse(artistId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTIST_NOT_FOUND));

        SortType sortType = SortType.RELEASE_DATE;
        SortDirection direction = sortType.getDefaultDirection();

        List<SongArtistDetailResponseDto> content = songRepository.findSongByArtistKeyset(authUser, foundArtist.getArtistId(), sortType, direction, cursor, DETAIL_SIZE);

        return KeysetResponse.of(content, DETAIL_SIZE, last -> new NextCursor(last.getSongId(), last.getAlbumReleaseDate()));
    }

    /**
     * 음원 검색 - 자세히 보기
     */
    @Transactional(readOnly = true)
    public KeysetResponse<SongSearchResponseDto> searchSongPage(AuthUser authUser, SearchConditionParam condition, CursorParam cursor) {

        List<SongSearchResponseDto> content = songRepository.findSongKeysetPageByWord(authUser, condition.getWord(), DETAIL_SIZE, PUBLIC_VIEW, condition.getSortType(), condition.getDirection(), cursor);

        return KeysetResponse.of(content, DETAIL_SIZE, last -> last.toCursor(condition.getSortType()));
    }

    /**
     * 음원 검색 - 미리보기
     */
    @Transactional(readOnly = true)
    public List<SongSearchResponseDto> searchSongList(AuthUser authUser, String word) {
        return songRepository.findSongListByWord(authUser, word, PREVIEW_SIZE, PUBLIC_VIEW, LIKE, DESC); // 좋아요 많은 순
    }

    /**
     * 음원 재생
     */
    @RedisLock(key = "song")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SongPlayResponseDto playSong(Long songId) {

        Song song = songRepository.findById(songId).orElseThrow(() -> new CustomException(ErrorCode.SONG_NOT_FOUND));

        String streamingUrl = streamingBaseUrl + "/" + song.getAudio();

        String key =  MUSIC_DAILY_KEY + LocalDate.now(); // 키에 날짜 반영
        String value = song.getName() + ":" + songId; // 레디스에 이름과 id 동시 저장을 위해 조합

        // Redis에 저장
        redisTemplate.opsForZSet().incrementScore(key, value ,1);
        redisTemplate.expire(key, Duration.ofDays(RedisResetTime.RESET_DATE));

        // DB에 저장
        song.addPlayCount();
        songRepository.save(song);

        return SongPlayResponseDto.from(streamingUrl);
    }
}