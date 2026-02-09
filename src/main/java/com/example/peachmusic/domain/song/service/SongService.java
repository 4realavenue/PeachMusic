package com.example.peachmusic.domain.song.service;

import com.example.peachmusic.common.annotation.RedisLock;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.model.*;
import com.example.peachmusic.common.service.KeysetPolicy;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.song.dto.response.SongArtistDetailResponseDto;
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
import java.util.EnumSet;
import java.util.List;
import static com.example.peachmusic.common.constants.SearchViewSize.*;
import static com.example.peachmusic.common.constants.UserViewScope.PUBLIC_VIEW;
import static com.example.peachmusic.common.enums.SortDirection.DESC;
import static com.example.peachmusic.common.enums.SortType.*;

@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;
    private final SongGenreRepository songGenreRepository;
    private final AlbumRepository albumRepository;
    private final SongLikeRepository songLikeRepository;
    private final ArtistRepository artistRepository;
    private final UserService userService;
    private final KeysetPolicy keysetPolicy;
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
     * 아티스트의 음원 자세히 보기
     */
    @Transactional(readOnly = true)
    public KeysetResponse<SongArtistDetailResponseDto> getArtistSongs(AuthUser authUser, Long artistId, CursorParam cursor) {

        Artist foundArtist = artistRepository.findByArtistIdAndIsDeleted(artistId, false)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTIST_DETAIL_NOT_FOUND));

        SortType sortType = SortType.RELEASE_DATE;
        keysetPolicy.validateCursor(sortType, cursor);

        final int size = DETAIL_SIZE;
        SortDirection direction = sortType.getDefaultDirection();

        List<SongArtistDetailResponseDto> content = songRepository.findSongByArtistKeyset(authUser.getUserId(), foundArtist.getArtistId(), sortType, direction, cursor, size);

        return KeysetResponse.of(content, size, last -> new NextCursor(last.getAlbumId(), last.getAlbumReleaseDate()));
    }

    /**
     * 음원 검색 - 자세히 보기
     */
    @Transactional(readOnly = true)
    public KeysetResponse<SongSearchResponseDto> searchSongPage(SearchConditionParam condition) {

        if (!EnumSet.of(LIKE, NAME, RELEASE_DATE, PLAY).contains(condition.getSortType())) {
            throw new CustomException(ErrorCode.UNSUPPORTED_SORT_TYPE);
        }

        CursorParam cursor = condition.getCursor();
        keysetPolicy.validateCursor(condition.getSortType(), cursor); // 커서 검증

        String[] words = condition.getWord().split("\\s+");
        final int size = DETAIL_SIZE;
        SortDirection direction = keysetPolicy.resolveSortDirection(condition.getSortType(), condition.getDirection());

        List<SongSearchResponseDto> content = songRepository.findSongKeysetPageByWord(words, size, PUBLIC_VIEW, condition.getSortType(), direction, cursor);

        return KeysetResponse.of(content, size, last -> last.toCursor(condition.getSortType()));
    }

    /**
     * 음원 검색 - 미리보기
     */
    @Transactional(readOnly = true)
    public List<SongSearchResponseDto> searchSongList(String word) {
        String[] words = word.split("\\s+");
        return songRepository.findSongListByWord(words, PREVIEW_SIZE, PUBLIC_VIEW, LIKE, DESC); // 좋아요 많은 순
    }

    /**
     * 음원 재생
     */
    @RedisLock(key = "song")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void play(Long songId) {

        LocalDate currentDate = LocalDate.now();
        Song song = songRepository.findById(songId).orElseThrow(() -> new CustomException(ErrorCode.SONG_NOT_FOUND));

        // 키에 날짜 반영
        String key =  MUSIC_DAILY_KEY + currentDate.toString();
        // music:2025-02-04

        // Redis에 저장
        redisTemplate.opsForZSet().incrementScore(key, songId.toString(),1);

        // DB에 저장
        song.addPlayCount();
        songRepository.save(song);
    }
}