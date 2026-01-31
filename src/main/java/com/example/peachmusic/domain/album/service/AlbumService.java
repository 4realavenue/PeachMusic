package com.example.peachmusic.domain.album.service;

import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.common.model.Cursor;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.common.service.AbstractKeysetService;
import com.example.peachmusic.domain.album.dto.response.AlbumSearchResponseDto;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.dto.response.AlbumGetDetailResponseDto;
import com.example.peachmusic.domain.album.dto.response.ArtistSummaryDto;
import com.example.peachmusic.domain.album.dto.response.SongSummaryDto;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.albumlike.repository.AlbumLikeRepository;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artistalbum.entity.ArtistAlbum;
import com.example.peachmusic.domain.artistalbum.repository.ArtistAlbumRepository;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import static com.example.peachmusic.common.enums.SortDirection.DESC;
import static com.example.peachmusic.common.enums.SortType.LIKE;

@Service
@RequiredArgsConstructor
public class AlbumService extends AbstractKeysetService {

    private final AlbumRepository albumRepository;
    private final ArtistAlbumRepository artistAlbumRepository;
    private final SongRepository songRepository;
    private final AlbumLikeRepository albumLikeRepository;

    /**
     * 앨범 단건 조회 기능
     *
     * @param authUser 인증된 사용자 정보
     * @param albumId  조회할 앨범 ID
     * @return 조회한 앨범 정보
     */
    @Transactional(readOnly = true)
    public AlbumGetDetailResponseDto getAlbumDetail(AuthUser authUser, Long albumId) {

        Album foundAlbum = albumRepository.findByAlbumIdAndIsDeletedFalse(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_DETAIL_NOT_FOUND));

        boolean isLiked = false;

        if (authUser != null) {
            Long userId = authUser.getUserId();
            isLiked = albumLikeRepository.existsByUser_UserIdAndAlbum_AlbumId(userId, albumId);
        }

        List<ArtistAlbum> artistAlbumList = artistAlbumRepository.findAllByAlbum_AlbumIdAndArtist_IsDeletedFalse(albumId);
        List<ArtistSummaryDto> artistSummaryDtoList = new ArrayList<>();
        for (ArtistAlbum artistAlbum : artistAlbumList) {
            Artist artist = artistAlbum.getArtist();
            artistSummaryDtoList.add(new ArtistSummaryDto(artist.getArtistId(), artist.getArtistName()));
        }

        List<Song> songList = songRepository.findAllByAlbum_AlbumIdAndIsDeletedFalse(albumId);
        List<SongSummaryDto> songSummaryDtoList = new ArrayList<>();
        for (Song song : songList) {
            songSummaryDtoList.add(new SongSummaryDto(song.getPosition(), song.getSongId(), song.getName(), song.getDuration(), song.getLikeCount()));
        }

        return AlbumGetDetailResponseDto.from(foundAlbum, artistSummaryDtoList, songSummaryDtoList, isLiked);
    }

    /**
     * 앨범 검색 - 자세히 보기
     */
    @Transactional(readOnly = true)
    public KeysetResponse<AlbumSearchResponseDto> searchAlbumPage(String word, SortType sortType, SortDirection direction, Long lastId, Long lastLike, String lastName) {
        // 커서 검증
        validate(sortType, lastId, lastLike, lastName);

        final int size = 10;
        final boolean isAdmin = false;
        direction = resolveSortDirection(sortType, direction);

        // 앨범 조회
        List<AlbumSearchResponseDto> content = albumRepository.findAlbumKeysetPageByWord(word, size, isAdmin, sortType, direction, lastId, lastLike, lastName);

        // 정렬 기준에 따라 커서 결정
        Function<AlbumSearchResponseDto, Cursor> cursorExtractor = switch (sortType) {
            case LIKE -> last -> new Cursor(last.getAlbumId(), last.getLikeCount());
            case NAME -> last -> new Cursor(last.getAlbumId(), last.getAlbumName());
        };

        return toKeysetResponse(content, size, cursorExtractor);
    }

    /**
     * 앨범 검색 - 미리보기
     * @param word 검색어
     * @return 앨범 검색 응답 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<AlbumSearchResponseDto> searchAlbumList(String word) {
        final int size = 5;
        final boolean isAdmin = false;
        return albumRepository.findAlbumListByWord(word, size, isAdmin, LIKE, DESC); // 좋아요 많은 순
    }
}
