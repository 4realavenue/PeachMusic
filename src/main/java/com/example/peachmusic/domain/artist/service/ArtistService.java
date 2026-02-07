package com.example.peachmusic.domain.artist.service;

import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.Cursor;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.common.model.SearchConditionParam;
import com.example.peachmusic.common.service.KeysetPolicy;
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
import static com.example.peachmusic.common.constants.SearchViewSize.*;
import static com.example.peachmusic.common.constants.UserViewScope.PUBLIC_VIEW;
import static com.example.peachmusic.common.enums.SortDirection.DESC;
import static com.example.peachmusic.common.enums.SortType.LIKE;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final ArtistLikeRepository artistLikeRepository;
    private final SongRepository songRepository;
    private final AlbumRepository albumRepository;
    private final KeysetPolicy keysetPolicy;

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
        keysetPolicy.validateArtistCursor(sortType, lastId, lastDate);

        List<AlbumArtistDetailResponseDto> content = albumRepository.findAlbumByArtistKeyset(authUser.getUserId(), foundArtist.getArtistId(), sortType, SortDirection.DESC, lastId, lastDate, size);

        return KeysetResponse.of(content, size, last -> new Cursor(last.getAlbumId(), last.getAlbumReleaseDate()));
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
        keysetPolicy.validateArtistCursor(sortType, lastId, lastDate);

        List<SongArtistDetailResponseDto> content = songRepository.findSongByArtistKeyset(authUser.getUserId(), foundArtist.getArtistId(), sortType, SortDirection.DESC, lastId, lastDate, size);

        return KeysetResponse.of(content, size, last -> new Cursor(last.getAlbumId(), last.getAlbumReleaseDate()));
    }

    /**
     * 아티스트 검색 - 자세히 보기
     */
    @Transactional(readOnly = true)
    public KeysetResponse<ArtistSearchResponseDto> searchArtistPage(SearchConditionParam condition) {

        keysetPolicy.validateCursor(condition); // 커서 검증

        String[] words = condition.getWord().split("\\s+");
        final int size = DETAIL_SIZE;
        SortDirection direction = keysetPolicy.resolveSortDirection(condition.getSortType(), condition.getDirection());

        List<ArtistSearchResponseDto> content = artistRepository.findArtistKeysetPageByWord(words, size, PUBLIC_VIEW, condition.getSortType(), direction, condition.getLastId(), condition.getLastLike(), condition.getLastName());

        return KeysetResponse.of(content, size, last -> last.toCursor(condition.getSortType()));
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