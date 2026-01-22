package com.example.peachmusic.domain.album.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.dto.request.AlbumCreateRequestDto;
import com.example.peachmusic.domain.album.dto.request.AlbumUpdateRequestDto;
import com.example.peachmusic.domain.album.dto.request.ArtistAlbumUpdateRequestDto;
import com.example.peachmusic.domain.album.dto.response.*;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.artistAlbum.entity.ArtistAlbum;
import com.example.peachmusic.domain.artistAlbum.repository.ArtistAlbumRepository;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static com.example.peachmusic.common.enums.UserRole.ADMIN;

@Service
@RequiredArgsConstructor
public class AlbumAdminService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final ArtistAlbumRepository artistAlbumRepository;
    private final SongRepository songRepository;

    /**
     * 앨범 생성 기능 (관리자 전용)
     * @param requestDto 앨범 생성 요청 DTO
     * @return 생성된 앨범 정보
     */
    @Transactional
    public AlbumCreateResponseDto createAlbum(AlbumCreateRequestDto requestDto) {

        // 요청된 아티스트 ID 중복 제거
        List<Long> artistIds = requestDto.getArtistIds().stream().distinct().toList();

        List<Artist> artistList = artistRepository.findAllById(artistIds);

        // 요청한 아티스트 ID 중 존재하지 않는 항목이 있는지 검증
        if (artistList.size() != artistIds.size()) {
            throw new CustomException(ErrorCode.ARTIST_NOT_FOUND);
        }

        String albumName = requestDto.getAlbumName();
        LocalDate albumReleaseDate = requestDto.getAlbumReleaseDate();
        String albumImage = requestDto.getAlbumImage();

        if (albumImage != null) {
            if (albumRepository.existsByAlbumImageAndIsDeletedFalse(albumImage)) {
                throw new CustomException(ErrorCode.ALBUM_EXIST_IMAGE);
            }
        }

        albumRepository.findByAlbumNameAndAlbumReleaseDate(albumName, albumReleaseDate)
            .ifPresent(album -> {
                if (album.isDeleted()) {
                throw new CustomException(ErrorCode.ALBUM_EXIST_NAME_RELEASE_DATE_DELETED);
            }
            throw new CustomException(ErrorCode.ALBUM_EXIST_NAME_RELEASE_DATE);
        });

        Album album = new Album(albumName, albumReleaseDate, albumImage);
        Album savedAlbum = albumRepository.save(album);

        // 참여 아티스트와 앨범의 N:M 관계를 매핑 테이블(ArtistAlbum)에 저장
        List<ArtistAlbum> artistAlbumList = artistList.stream()
                .map(artist -> new ArtistAlbum(artist, savedAlbum))
                .toList();
        artistAlbumRepository.saveAll(artistAlbumList);

        // ArtistAlbum 기준으로 응답 DTO를 조립
        List<ArtistSummaryDto> dtoList = artistAlbumList.stream()
                .map(artist -> ArtistSummaryDto.from(artist.getArtist()))
                .toList();

        return AlbumCreateResponseDto.from(savedAlbum, dtoList);
    }

    /**
     * 전체 앨범 조회 기능 (관리자 전용)
     * @param pageable 페이지네이션 및 정렬 정보 (기본 정렬: albumId ASC)
     * @return 앨범 목록 페이징 조회 결과
     */
    @Transactional(readOnly = true)
    public Page<AlbumSearchResponseDto> getAlbumList(String word, Pageable pageable) {
        return albumRepository.findAlbumPageByWord(word, pageable, ADMIN);
    }

    /**
     * 앨범 기본 정보 수정 기능 (관리자 전용)
     * @param albumId 수정할 앨범 ID
     * @param requestDto 앨범 수정 요청 DTO
     * @return 수정된 앨범 정보
     */
    @Transactional
    public AlbumUpdateResponseDto updateAlbumInfo(Long albumId, AlbumUpdateRequestDto requestDto) {

        Album foundAlbum = albumRepository.findByAlbumIdAndIsDeletedFalse(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        if (!hasUpdateFields(requestDto)) {
            throw new CustomException(ErrorCode.ALBUM_UPDATE_NO_CHANGES);
        }

        String image = requestDto.getAlbumImage();

        // 이미지 유니크 검증 먼저 수행
        if (image != null && !image.isBlank()) {
            String trimmed = image.trim();
            if (albumRepository.existsByAlbumImageAndIsDeletedFalseAndAlbumIdNot(trimmed, albumId)) {
                throw new CustomException(ErrorCode.ALBUM_EXIST_IMAGE);
            }
        }

        foundAlbum.updateAlbumName(requestDto.getAlbumName());
        foundAlbum.updateAlbumReleaseDate(requestDto.getAlbumReleaseDate());
        foundAlbum.updateAlbumImage(requestDto.getAlbumImage());

        if (albumRepository.existsByAlbumNameAndAlbumReleaseDateAndIsDeletedFalseAndAlbumIdNot(
                   foundAlbum.getAlbumName(), foundAlbum.getAlbumReleaseDate(), albumId)) {
               throw new CustomException(ErrorCode.ALBUM_EXIST_NAME_RELEASE_DATE);
        }

        List<ArtistSummaryDto> artistList = artistAlbumRepository.findAllByAlbum_AlbumId(albumId).stream()
                .map(artist -> new ArtistSummaryDto(artist.getArtist().getArtistId(), artist.getArtist().getArtistName()))
                .toList();

        return AlbumUpdateResponseDto.from(foundAlbum, artistList);
    }

    private boolean hasUpdateFields(AlbumUpdateRequestDto requestDto) {
        return requestDto.getAlbumName() != null || requestDto.getAlbumReleaseDate() != null || requestDto.getAlbumImage() != null;
    }

    /**
     * 참여 아티스트 목록 전체 갱신 기능 (관리자 전용)
     * @param albumId 갱신할 앨범 ID
     * @param requestDto 참여 아티스트 수정 요청 DTO
     * @return 참여 아티스트가 반영된 앨범 정보
     */
    @Transactional
    public ArtistAlbumUpdateResponseDto updateAlbumArtistList(Long albumId, ArtistAlbumUpdateRequestDto requestDto) {

        // 수정 대상 앨범 조회 (삭제된 앨범은 수정 불가)
        Album foundAlbum = albumRepository.findByAlbumIdAndIsDeletedFalse(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        // 요청된 아티스트 ID 중복 제거
        List<Long> artistIds = requestDto.getArtistIds().stream().distinct().toList();

        List<Artist> artistList = artistRepository.findAllByArtistIdInAndIsDeletedFalse(artistIds);

        // 요청한 아티스트 ID 중 존재하지 않는 항목이 있는지 검증
        if (artistList.size() != artistIds.size()) {
            throw new CustomException(ErrorCode.ARTIST_NOT_FOUND);
        }

        // 앨범 정책에 따라 기존 매핑은 하드 딜리트 후 재생성
        artistAlbumRepository.deleteAllByAlbumId(foundAlbum.getAlbumId());

        List<ArtistAlbum> artistAlbumList = artistList.stream()
                .map(artist -> new ArtistAlbum(artist, foundAlbum))
                .toList();

        artistAlbumRepository.saveAll(artistAlbumList);

        // ArtistAlbum 기준으로 응답 DTO를 조립
        List<ArtistSummaryDto> dtoList = artistAlbumList.stream()
                .map(artist -> ArtistSummaryDto.from(artist.getArtist()))
                .toList();

        return ArtistAlbumUpdateResponseDto.from(foundAlbum, dtoList);
    }

    /**
     * 앨범 비활성화 기능 (관리자 전용)
     * @param albumId 비활성화할 앨범 ID
     */
    @Transactional
    public void deleteAlbum(Long albumId) {

        // 비활성화 대상 앨범 조회 (이미 비활성화된 앨범은 제외)
        Album foundAlbum = albumRepository.findByAlbumIdAndIsDeletedFalse(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_DETAIL_NOT_FOUND));

        // 앨범에 포함된 음원 비활성화
        List<Song> foundSongList = songRepository.findAllByAlbum_AlbumIdAndIsDeletedFalse(albumId);

        foundSongList.forEach(Song::deleteSong);

        // 앨범 비활성화
        foundAlbum.delete();
    }

    /**
     * 앨범 활성화 기능 (관리자 전용)
     * @param albumId 활성화할 앨범 ID
     */
    @Transactional
    public void restoreAlbum(Long albumId) {

        // 활성화 대상 앨범 조회 (삭제된 앨범만 복구 가능)
        Album foundAlbum = albumRepository.findByAlbumIdAndIsDeletedTrue(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_DETAIL_NOT_FOUND));

        // 앨범에 포함된 음원 활성화
        List<Song> foundSongList = songRepository.findAllByAlbum_AlbumIdAndIsDeletedTrue(albumId);

        foundSongList.forEach(Song::restoreSong);

        // 앨범 활성화
        foundAlbum.restore();
    }
}
