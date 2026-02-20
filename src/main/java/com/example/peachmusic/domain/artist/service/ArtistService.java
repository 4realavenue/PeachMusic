package com.example.peachmusic.domain.artist.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.model.*;
import com.example.peachmusic.domain.album.dto.response.AlbumArtistDetailResponseDto;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.artist.dto.response.ArtistPreviewResponseDto;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.dto.response.ArtistGetDetailResponseDto;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.artist.dto.response.ArtistSearchResponseDto;
import com.example.peachmusic.domain.artistlike.service.ArtistLikeService;
import com.example.peachmusic.domain.song.dto.response.SongArtistDetailResponseDto;
import com.example.peachmusic.domain.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static com.example.peachmusic.common.constants.SearchViewSize.*;
import static com.example.peachmusic.common.constants.UserViewScope.PUBLIC_VIEW;
import static com.example.peachmusic.common.enums.SortDirection.DESC;
import static com.example.peachmusic.common.enums.SortType.LIKE;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final SongRepository songRepository;
    private final AlbumRepository albumRepository;
    private final ArtistLikeService artistLikeService;

    /**
     * 아티스트 단건 조회 기능
     * @param artistId 조회할 아티스트 ID
     * @return 조회한 아티스트 정보
     */
    @Transactional(readOnly = true)
    public ArtistGetDetailResponseDto getArtistDetail(AuthUser authUser, Long artistId) {

        Artist foundArtist = findActiveArtistOrThrow(artistId);

        boolean isLiked = false;

        if (authUser != null) {
            Long userId = authUser.getUserId();
            isLiked = artistLikeService.isArtistLiked(artistId, userId);
        }

        return ArtistGetDetailResponseDto.from(foundArtist, isLiked);
    }

    /**
     * 아티스트의 앨범 및 음원 미리보기
     */
    @Transactional(readOnly = true)
    public ArtistPreviewResponseDto getArtistDetailPreview(AuthUser authUser, Long artistId) {

        Artist foundArtist = findActiveArtistOrThrow(artistId);

        int size = PREVIEW_SIZE;

        List<AlbumArtistDetailResponseDto> albumList = albumRepository.findAlbumList(authUser, foundArtist.getArtistId(), size);
        List<SongArtistDetailResponseDto> songList = songRepository.findSongList(authUser, foundArtist.getArtistId(), size);

        return ArtistPreviewResponseDto.of(albumList, songList);
    }

    /**
     * 아티스트 검색 - 자세히 보기
     */
    @Transactional(readOnly = true)
    public KeysetResponse<ArtistSearchResponseDto> searchArtistPage(AuthUser authUser, SearchConditionParam condition, CursorParam cursor) {

        List<ArtistSearchResponseDto> content = artistRepository.findArtistKeysetPageByWord(authUser, condition.getWord(), DETAIL_SIZE, PUBLIC_VIEW, condition.getSortType(), condition.getDirection(), cursor);

        return KeysetResponse.of(content, DETAIL_SIZE, last -> last.toCursor(condition.getSortType()));
    }

    /**
     * 아티스트 검색 - 미리보기
     * @param word 검색어
     * @return 아티스트 검색 응답 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<ArtistSearchResponseDto> searchArtistList(AuthUser authUser, String word) {
        return artistRepository.findArtistListByWord(authUser, word, PREVIEW_SIZE, PUBLIC_VIEW, LIKE, DESC);
    }

    public Artist findActiveArtistOrThrow(Long artistId) {
        return artistRepository.findByArtistIdAndIsDeletedFalse(artistId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTIST_NOT_FOUND));
    }
}