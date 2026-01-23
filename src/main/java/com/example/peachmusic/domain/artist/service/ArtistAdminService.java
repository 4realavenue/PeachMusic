package com.example.peachmusic.domain.artist.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.dto.request.ArtistCreateRequestDto;
import com.example.peachmusic.domain.artist.dto.request.ArtistUpdateRequestDto;
import com.example.peachmusic.domain.artist.dto.response.ArtistCreateResponseDto;
import com.example.peachmusic.domain.artist.dto.response.ArtistSearchResponseDto;
import com.example.peachmusic.domain.artist.dto.response.ArtistUpdateResponseDto;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.artistAlbum.repository.ArtistAlbumRepository;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import static com.example.peachmusic.common.enums.UserRole.ADMIN;

@Service
@RequiredArgsConstructor
public class ArtistAdminService {

    private final ArtistRepository artistRepository;
    private final SongRepository songRepository;
    private final ArtistAlbumRepository artistAlbumRepository;

    /**
     * 아티스트 생성 기능 (관리자 전용)
     * @param requestDto 아티스트 생성 요청 DTO
     * @return 생성된 아티스트 정보
     */
    @Transactional
    public ArtistCreateResponseDto createArtist(ArtistCreateRequestDto requestDto) {

        String artistName = requestDto.getArtistName().trim();

        artistRepository.findByArtistName(artistName)
                .ifPresent(artist -> {
                    if (artist.isDeleted()) {
                        throw new CustomException(ErrorCode.ARTIST_EXIST_NAME_DELETED);
                    }
                    throw new CustomException(ErrorCode.ARTIST_EXIST_NAME);
                });

        Artist artist = new Artist(artistName);
        Artist savedArtist = artistRepository.save(artist);

        return ArtistCreateResponseDto.from(savedArtist);
    }

    /**
     * 전체 아티스트 조회 기능 (관리자 전용)
     * @param pageable 페이지네이션 및 정렬 정보 (기본 정렬: artistId ASC)
     * @return 아티스트 목록 페이징 조회 결과
     */
    @Transactional(readOnly = true)
    public Page<ArtistSearchResponseDto> getArtistList(String word, Pageable pageable) {
        return artistRepository.findArtistPageByWord(word, pageable, ADMIN);
    }

    /**
     * 아티스트 수정 기능 (관리자 전용)
     * @param artistId 수정할 아티스트 ID
     * @param requestDto 아티스트 수정 요청 DTO
     * @return 수정된 아티스트 정보
     */
    @Transactional
    public ArtistUpdateResponseDto updateArtist(Long artistId, ArtistUpdateRequestDto requestDto) {

        Artist foundArtist = artistRepository.findByArtistIdAndIsDeletedFalse(artistId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTIST_NOT_FOUND));

        String newName = requestDto.getArtistName();

        // 입력값을 공통 기준으로 정규화(trim)하여
        // 변경 여부 판단, 중복 검증, 업데이트에 동일하게 사용
        String trimmed = (newName == null) ? null : newName.trim();

        if (foundArtist.isSameName(trimmed)) {
            return ArtistUpdateResponseDto.from(foundArtist);
        }

        boolean exists = artistRepository.existsByArtistNameAndIsDeletedFalseAndArtistIdNot(trimmed, artistId);
        if (exists) {
            throw new CustomException(ErrorCode.ARTIST_EXIST_NAME);
        }

        foundArtist.updateArtistName(trimmed);

        return ArtistUpdateResponseDto.from(foundArtist);
    }

    /**
     * 아티스트 비활성화 기능 (관리자 전용)
     * @param artistId 비활성화할 아티스트 ID
     */
    @Transactional
    public void deleteArtist(Long artistId) {

        Artist foundArtist = artistRepository.findByArtistIdAndIsDeletedFalse(artistId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTIST_DETAIL_NOT_FOUND));

        List<Album> foundAlbumList = artistAlbumRepository.findAlbumsByArtistIdAndIsDeleted(artistId, false);

        List<Long> albumIdList = foundAlbumList.stream()
                .map(Album::getAlbumId)
                .toList();

        if (!albumIdList.isEmpty()) {
            List<Song> foundSongList = songRepository.findAllByAlbum_AlbumIdInAndIsDeletedFalse(albumIdList);
            foundSongList.forEach(Song::deleteSong);
        }

        foundAlbumList.forEach(Album::delete);

        foundArtist.delete();
    }

    /**
     * 아티스트 활성화 기능 (관리자 전용)
     * @param artistId 활성화할 아티스트 ID
     */
    @Transactional
    public void restoreArtist(Long artistId) {

        Artist foundArtist = artistRepository.findByArtistIdAndIsDeletedTrue(artistId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTIST_DETAIL_NOT_FOUND));

        List<Album> foundAlbumList = artistAlbumRepository.findAlbumsByArtistIdAndIsDeleted(artistId, true);

        List<Long> albumIdList = foundAlbumList.stream()
                .map(Album::getAlbumId)
                .toList();

        if (!albumIdList.isEmpty()) {
            List<Song> foundSongList = songRepository.findAllByAlbum_AlbumIdInAndIsDeletedTrue(albumIdList);
            foundSongList.forEach(Song::restoreSong);
        }

        foundAlbumList.forEach(Album::restore);

        foundArtist.restore();
    }
}
