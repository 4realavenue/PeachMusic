package com.example.peachmusic.domain.song.service;

import com.example.peachmusic.common.annotation.RedisLock;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.Cursor;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.common.service.AbstractKeysetService;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.song.dto.response.SongGetDetailResponseDto;
import com.example.peachmusic.domain.song.dto.response.SongSearchResponseDto;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.repository.SongRepository;
import com.example.peachmusic.domain.songgenre.entity.SongGenre;
import com.example.peachmusic.domain.songgenre.repository.SongGenreRepository;
import com.example.peachmusic.domain.songlike.repository.SongLikeRepository;
import com.example.peachmusic.domain.user.entity.User;
import com.example.peachmusic.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

import static com.example.peachmusic.common.enums.SortDirection.DESC;
import static com.example.peachmusic.common.enums.SortType.LIKE;
import static com.example.peachmusic.common.enums.SortType.NAME;

@Service
@RequiredArgsConstructor
public class SongService extends AbstractKeysetService {

    private final SongRepository songRepository;
    private final SongGenreRepository songGenreRepository;
    private final AlbumRepository albumRepository;
    private final SongLikeRepository songLikeRepository;
    private final UserService userService;
    private final RedisTemplate<String,String> redisTemplate;

    public static final String MUSIC_DAILY_KEY = "music";

    /**
     * 음원 단건 조회
     */
    @Transactional(readOnly = true)
    public SongGetDetailResponseDto getSong(Long songId, AuthUser authUser) {

        Song findSong = songRepository.findBySongIdAndIsDeletedFalse(songId)
                .orElseThrow(() -> new CustomException(ErrorCode.SONG_NOT_FOUND));

        boolean liked = false;

        if (authUser != null) { // 로그인이 된 경우
            User findUser = userService.findUser(authUser);
            if (songLikeRepository.existsSongLikeByUserAndSong(findUser, findSong)) {
                liked = true;
            }
        }

        Long findAlbumId = songRepository.findSongs_AlbumIdBySongId(findSong);

        Album findAlbum = albumRepository.findByAlbumIdAndIsDeletedFalse(findAlbumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        List<SongGenre> findSongGenreList = songGenreRepository.findAllBySong(findSong);

        List<String> genreNameList = findSongGenreList.stream()
                .map(songGenre -> songGenre.getGenre().getGenreName())
                .toList();

        return SongGetDetailResponseDto.from(findSong, genreNameList, findAlbum, liked);

    }

    /**
     * 음원 검색 - 자세히 보기
     */
    @Transactional(readOnly = true)
    public KeysetResponse<SongSearchResponseDto> searchSongPage(String word, SortType sortType, SortDirection direction, Long lastId, Long lastLike, String lastName) {

        validateWord(word); // 단어 검증
        if (!sortType.equals(LIKE) && !sortType.equals(NAME)) { // 정렬 기준 검증
            throw new CustomException(ErrorCode.UNSUPPORTED_SORT_TYPE);
        }
        validateCursor(sortType, lastId, lastLike, lastName); // 커서 검증

        String[] words = word.split("\\s+");
        final int size = 10;
        final boolean isAdmin = false;
        direction = resolveSortDirection(sortType, direction);

        // 음원 조회
        List<SongSearchResponseDto> content = songRepository.findSongKeysetPageByWord(words, size, isAdmin, sortType, direction, lastId, lastLike, lastName);

        // 정렬 기준에 따라 커서 결정
        Function<SongSearchResponseDto, Cursor> cursorExtractor = switch (sortType) {
            case LIKE -> last -> new Cursor(last.getSongId(), last.getLikeCount());
            case NAME -> last -> new Cursor(last.getSongId(), last.getName());
            default -> throw new CustomException(ErrorCode.UNSUPPORTED_SORT_TYPE);
        };

        return toKeysetResponse(content, size, cursorExtractor);
    }

    /**
     * 음원 검색 - 미리보기
     */
    @Transactional(readOnly = true)
    public List<SongSearchResponseDto> searchSongList(String word) {
        String[] words = word.split("\\s+");
        final int size = 5;
        final boolean isAdmin = false;
        return songRepository.findSongListByWord(words, size, isAdmin, LIKE, DESC); // 좋아요 많은 순
    }

    /**
     * 음원 재생
     */
    @RedisLock(key = "song")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void play( Long songId) {

        LocalDate currentDate = LocalDate.now();
        Song song = songRepository.findById(songId).orElseThrow(() -> new CustomException(ErrorCode.SONG_NOT_FOUND));

        // 키에 날짜 반영
        String key =  MUSIC_DAILY_KEY + currentDate.toString();
        // music:2025-02-04

        // Redis에 저장
        redisTemplate.opsForZSet().incrementScore(key, songId.toString(),1);

        // DB에 저장
        song.playcount();
        songRepository.save(song);
    }
}