package com.example.peachmusic.domain.album.service;

import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.model.*;
import com.example.peachmusic.domain.album.dto.response.*;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.albumlike.service.AlbumLikeService;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.artistalbum.repository.ArtistAlbumRepository;
import com.example.peachmusic.domain.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static com.example.peachmusic.common.constants.SearchViewSize.*;
import static com.example.peachmusic.common.constants.UserViewScope.PUBLIC_VIEW;
import static com.example.peachmusic.common.enums.SortDirection.DESC;
import static com.example.peachmusic.common.enums.SortType.*;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistAlbumRepository artistAlbumRepository;
    private final SongRepository songRepository;
    private final AlbumLikeService albumLikeService;
    private final ArtistRepository artistRepository;

    /**
     * 앨범 단건 조회 기능
     *
     * @param authUser 인증된 사용자 정보
     * @param albumId  조회할 앨범 ID
     * @return 조회한 앨범 정보
     */
    @Transactional(readOnly = true)
    public AlbumGetDetailResponseDto getAlbumDetail(AuthUser authUser, Long albumId) {

        Album foundAlbum = albumRepository.findActiveAlbumWithActiveSong(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        boolean isLiked = false;

        if (authUser != null) {
            Long userId = authUser.getUserId();
            isLiked = albumLikeService.isAlbumLiked(albumId, userId);
        }

        List<ArtistSummaryDto> artistAlbumList = artistAlbumRepository.findArtistSummaryListByAlbumId(albumId);

        List<SongSummaryDto> songList = songRepository.findSongSummaryListByAlbumId(albumId);

        return AlbumGetDetailResponseDto.from(foundAlbum, artistAlbumList, songList, isLiked);
    }

    /**
     * 아티스트의 앨범 자세히 보기
     */
    @Transactional(readOnly = true)
    public KeysetResponse<AlbumArtistDetailResponseDto> getArtistAlbums(AuthUser authUser, Long artistId, CursorParam cursor) {

        Artist foundArtist = artistRepository.findByArtistIdAndIsDeletedFalse(artistId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTIST_NOT_FOUND));

        SortType sortType = SortType.RELEASE_DATE;
        SortDirection direction = sortType.getDefaultDirection();

        List<AlbumArtistDetailResponseDto> content = albumRepository.findAlbumByArtistKeyset(authUser, foundArtist.getArtistId(), sortType, direction, cursor, DETAIL_SIZE);

        return KeysetResponse.of(content, DETAIL_SIZE, last -> new NextCursor(last.getAlbumId(), last.getAlbumReleaseDate()));
    }

    /**
     * 앨범 검색 - 자세히 보기
     */
    @Transactional(readOnly = true)
    public KeysetResponse<AlbumSearchResponseDto> searchAlbumPage(AuthUser authUser, SearchConditionParam condition, CursorParam cursor) {

        List<AlbumSearchResponseDto> content = albumRepository.findAlbumKeysetPageByWord(authUser, condition.getWord(), DETAIL_SIZE, PUBLIC_VIEW, condition.getSortType(), condition.getDirection(), cursor);

        return KeysetResponse.of(content, DETAIL_SIZE, last -> last.toCursor(condition.getSortType()));
    }

    /**
     * 앨범 검색 - 미리보기
     * @param word 검색어
     * @return 앨범 검색 응답 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<AlbumSearchResponseDto> searchAlbumList(AuthUser authUser, String word) {
        return albumRepository.findAlbumListByWord(authUser, word, PREVIEW_SIZE, PUBLIC_VIEW, LIKE, DESC); // 좋아요 많은 순
    }
}
