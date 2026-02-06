package com.example.peachmusic.domain.artist.service;

import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.Cursor;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.common.service.AbstractKeysetService;
import com.example.peachmusic.domain.album.dto.response.AlbumArtistDetailResponseDto;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.artist.dto.response.ArtistPreviewResponseDto;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.dto.response.ArtistGetDetailResponseDto;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.artist.dto.response.ArtistSearchResponseDto;
import com.example.peachmusic.domain.artistlike.repository.ArtistLikeRepository;
import com.example.peachmusic.domain.song.dto.response.SongArtistDetailResponseDto;
import com.example.peachmusic.domain.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import static com.example.peachmusic.common.constants.SearchViewSize.*;
import static com.example.peachmusic.common.constants.UserViewScope.PUBLIC_VIEW;
import static com.example.peachmusic.common.enums.SortDirection.DESC;
import static com.example.peachmusic.common.enums.SortType.LIKE;
import static com.example.peachmusic.common.enums.SortType.NAME;

@Service
@RequiredArgsConstructor
public class ArtistService extends AbstractKeysetService {

    private final ArtistRepository artistRepository;
    private final ArtistLikeRepository artistLikeRepository;
    private final SongRepository songRepository;
    private final AlbumRepository albumRepository;

    /**
     * 아티스트 단건 조회 기능
     * @param artistId 조회할 아티스트 ID
     * @return 조회한 아티스트 정보
     */
    @Transactional(readOnly = true)
    public ArtistGetDetailResponseDto getArtistDetail(AuthUser authUser, Long artistId) {

        Artist foundArtist = artistRepository.findByArtistIdAndIsDeleted(artistId, false)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTIST_DETAIL_NOT_FOUND));

        boolean isLiked = false;

        if (authUser != null) {
            Long userId = authUser.getUserId();
            isLiked = artistLikeRepository.existsByArtist_ArtistIdAndUser_UserId(artistId, userId);
        }

        return ArtistGetDetailResponseDto.from(foundArtist, isLiked);
    }

    /**
     * 아티스트의 앨범 및 음원 미리보기
     */
    @Transactional(readOnly = true)
    public ArtistPreviewResponseDto getArtistDetailPreview(AuthUser authUser, Long artistId) {
        Artist foundArtist = artistRepository.findByArtistIdAndIsDeleted(artistId, false)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTIST_DETAIL_NOT_FOUND));

        int size = PREVIEW_SIZE;
        List<AlbumArtistDetailResponseDto> albumList = albumRepository.findAlbumList(authUser.getUserId(), foundArtist.getArtistId(), size);
        List<SongArtistDetailResponseDto> songList = songRepository.findSongList(authUser.getUserId(), foundArtist.getArtistId(), size);

        return ArtistPreviewResponseDto.of(albumList, songList);
    }

    /**
     * 아티스트의 앨범 자세히 보기
     */
    @Transactional(readOnly = true)
    public KeysetResponse<AlbumArtistDetailResponseDto> getArtistAlbums(AuthUser authUser, Long artistId, Long lastId, LocalDate lastDate) {
        Artist foundArtist = artistRepository.findByArtistIdAndIsDeleted(artistId, false)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTIST_DETAIL_NOT_FOUND));

        final int size = DETAIL_SIZE;
        SortType sortType = SortType.RELEASE_DATE;
        validateArtistCursor(sortType, lastId, lastDate);

        List<AlbumArtistDetailResponseDto> content = albumRepository.findAlbumByArtistKeyset(authUser.getUserId(), foundArtist.getArtistId(), sortType, SortDirection.DESC, lastId, lastDate, size);

        return toKeysetResponse(content, size, last -> new Cursor(last.getAlbumId(), last.getAlbumReleaseDate()));
    }

    /**
     * 아티스트의 음원 자세히 보기
     */
    @Transactional(readOnly = true)
    public KeysetResponse<SongArtistDetailResponseDto> getArtistSongs(AuthUser authUser, Long artistId, Long lastId, LocalDate lastDate) {
        Artist foundArtist = artistRepository.findByArtistIdAndIsDeleted(artistId, false)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTIST_DETAIL_NOT_FOUND));

        final int size = DETAIL_SIZE;
        SortType sortType = SortType.RELEASE_DATE;
        validateArtistCursor(sortType, lastId, lastDate);

        List<SongArtistDetailResponseDto> content = songRepository.findSongByArtistKeyset(authUser.getUserId(), foundArtist.getArtistId(), sortType, SortDirection.DESC, lastId, lastDate, size);

        return toKeysetResponse(content, size, last -> new Cursor(last.getAlbumId(), last.getAlbumReleaseDate()));
    }

    /**
     * 아티스트 검색 - 자세히 보기
     */
    @Transactional(readOnly = true)
    public KeysetResponse<ArtistSearchResponseDto> searchArtistPage(String word, SortType sortType, SortDirection direction, Long lastId, Long lastLike, String lastName) {

        validateWord(word); // 단어 검증
        if (!sortType.equals(LIKE) && !sortType.equals(NAME)) { // 정렬 기준 검증
            throw new CustomException(ErrorCode.UNSUPPORTED_SORT_TYPE);
        }
        validateCursor(sortType, lastId, lastLike, lastName); // 커서 검증

        String[] words = word.split("\\s+");
        final int size = DETAIL_SIZE;
        direction = resolveSortDirection(sortType, direction);

        // 아티스트 조회
        List<ArtistSearchResponseDto> content = artistRepository.findArtistKeysetPageByWord(words, size, PUBLIC_VIEW, sortType, direction, lastId, lastLike, lastName);

        // 정렬 기준에 따라 커서 결정
        Function<ArtistSearchResponseDto, Cursor> cursorExtractor = switch (sortType) {
            case LIKE -> last -> new Cursor(last.getArtistId(), last.getLikeCount());
            case NAME -> last -> new Cursor(last.getArtistId(), last.getArtistName());
            default -> throw new CustomException(ErrorCode.UNSUPPORTED_SORT_TYPE);
        };

        return toKeysetResponse(content, size, cursorExtractor);
    }

    /**
     * 아티스트 검색 - 미리보기
     * @param word 검색어
     * @return 아티스트 검색 응답 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<ArtistSearchResponseDto> searchArtistList(String word) {
        String[] words = word.split("\\s+");
        return artistRepository.findArtistListByWord(words, PREVIEW_SIZE, PUBLIC_VIEW, LIKE, DESC);
    }
}