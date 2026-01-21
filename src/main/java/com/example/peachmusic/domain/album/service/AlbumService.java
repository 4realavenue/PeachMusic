package com.example.peachmusic.domain.album.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.model.response.AlbumGetDetailResponseDto;
import com.example.peachmusic.domain.album.model.response.ArtistSummaryDto;
import com.example.peachmusic.domain.album.model.response.SongSummaryDto;
import com.example.peachmusic.domain.album.model.response.AlbumSearchResponse;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artistAlbum.entity.ArtistAlbum;
import com.example.peachmusic.domain.artistAlbum.repository.ArtistAlbumRepository;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import static com.example.peachmusic.common.enums.UserRole.USER;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistAlbumRepository artistAlbumRepository;
    private final SongRepository songRepository;

    /**
     * 앨범 단건 조회 기능
     * @param albumId 조회할 앨범 ID
     * @return 조회한 앨범 정보
     */
    @Transactional(readOnly = true)
    public AlbumGetDetailResponseDto getAlbumDetail(Long albumId) {

        // 조회 대상 앨범 조회 (삭제된 앨범은 조회 불가)
        Album foundAlbum = albumRepository.findByAlbumIdAndIsDeletedFalse(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        // 참여 아티스트 조회 -> DTO 변환
        List<ArtistAlbum> artistAlbumList = artistAlbumRepository.findAllByAlbum_AlbumId(albumId);
        List<ArtistSummaryDto> artists = new ArrayList<>();
        for (ArtistAlbum artistAlbum : artistAlbumList) {
            Artist artist = artistAlbum.getArtist();
            artists.add(new ArtistSummaryDto(artist.getArtistId(), artist.getArtistName()));
        }

        // 앨범 음원 조회 -> DTO 변환
        List<Song> songList = songRepository.findAllByAlbum_AlbumIdAndIsDeletedFalse(albumId);
        List<SongSummaryDto> songs = new ArrayList<>();
        for (Song song : songList) {
            songs.add(new SongSummaryDto(song.getSongId(), song.getName(), song.getDuration(), song.getPosition()));
        }

        return AlbumGetDetailResponseDto.from(foundAlbum, artists, songs);
    }

    /**
     * 앨범 검색 - 자세히 보기
     * @param word 검색어
     * @param pageable 페이징 정보 - 인기순 정렬
     * @return 페이징된 앨범 검색 응답 DTO
     */
    @Transactional(readOnly = true)
    public Page<AlbumSearchResponse> searchAlbumPage(String word, Pageable pageable) {
        return albumRepository.findAlbumPageByWord(word, pageable, USER);
    }

    /**
     * 앨범 검색 - 미리보기
     * @param word 검색어
     * @return 앨범 검색 응답 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<AlbumSearchResponse> searchAlbumList(String word) {
        final int limit = 5;
        return albumRepository.findAlbumListByWord(word, limit);
    }
}
