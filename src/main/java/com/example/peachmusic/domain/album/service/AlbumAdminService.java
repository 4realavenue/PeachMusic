package com.example.peachmusic.domain.album.service;

import com.example.peachmusic.common.enums.UserRole;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.exception.ErrorCode;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.model.request.AlbumCreateRequestDto;
import com.example.peachmusic.domain.album.model.request.AlbumUpdateRequestDto;
import com.example.peachmusic.domain.album.model.request.ArtistAlbumUpdateRequestDto;
import com.example.peachmusic.domain.album.model.response.*;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.artistAlbum.entity.ArtistAlbum;
import com.example.peachmusic.domain.artistAlbum.repository.ArtistAlbumRepository;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.repository.SongRepository;
import com.example.peachmusic.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlbumAdminService {

    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;
    private final ArtistAlbumRepository artistAlbumRepository;
    private final SongRepository songRepository;

    /**
     * 앨범 생성 기능 (관리자 전용)
     * JWT 적용 전 단계로, 사용자 식별 정보와 권한을 파라미터로 임시 전달받아 처리한다.
     *
     * @param userId 사용자 ID (JWT 적용 전까지 임시 사용)
     * @param role 사용자 권한
     * @param requestDto 앨범 생성 요청 DTO
     * @return 생성된 앨범 정보
     */
    @Transactional
    public AlbumCreateResponseDto createAlbum(Long userId, UserRole role, AlbumCreateRequestDto requestDto) {

        // 삭제되지 않은 유효한 사용자 여부 검증
        userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_CERTIFICATION_REQUIRED));

        // 관리자(ADMIN) 권한 여부 검증
        if (role != UserRole.ADMIN) {
            throw new CustomException(ErrorCode.AUTH_AUTHORIZATION_REQUIRED);
        }

        List<Long> artistIds = requestDto.getArtistIds();

        List<Artist> artistList = artistRepository.findAllById(artistIds);

        // 요청한 아티스트 ID 중 존재하지 않는 항목이 있는지 검증
        if (artistList.size() != artistIds.size()) {
            throw new CustomException(ErrorCode.ARTIST_NOT_FOUND);
        }

        String albumName = requestDto.getAlbumName();
        LocalDate albumReleaseDate = requestDto.getAlbumReleaseDate();

        Optional<Album> found = albumRepository.findByAlbumNameAndAlbumReleaseDate(albumName, albumReleaseDate);

        // 동일한 앨범이 이미 존재하는 경우
        if (found.isPresent()) {
            Album album = found.get();

            // 비활성 상태(isDeleted=true)인 경우: 복구 대상이므로 생성 불가
            if (album.isDeleted()) {
                throw new CustomException(ErrorCode.ALBUM_EXIST_NAME_RELEASE_DATE_DELETED);
            }

            // 활성 상태(isDeleted=false)인 경우: 중복 앨범으로 생성 불가
            throw new CustomException(ErrorCode.ALBUM_EXIST_NAME_RELEASE_DATE);
        }

        // 요청 값으로 앨범 엔티티 생성 및 저장
        Album album = new Album(albumName, albumReleaseDate, requestDto.getAlbumImage());
        Album savedAlbum = albumRepository.save(album);

        // 참여 아티스트와 앨범의 N:M 관계를 매핑 테이블(ArtistAlbum)에 저장
        List<ArtistAlbum> artistAlbumList = artistList.stream()
                .map(artist -> new ArtistAlbum(artist, savedAlbum))
                .toList();
        artistAlbumRepository.saveAll(artistAlbumList);

        // 응답에 필요한 아티스트 정보만 DTO로 변환
        List<ArtistSummaryDto> dtoList = artistList.stream()
                .map(artist -> new ArtistSummaryDto(artist.getArtistId(), artist.getArtistName()))
                .toList();

        return AlbumCreateResponseDto.from(savedAlbum, dtoList);
    }

    /**
     * 전체 앨범 조회 기능 (관리자 전용)
     * @param userId 사용자 ID (JWT 적용 전까지 임시 사용)
     * @param role 사용자 권한
     * @param pageable 페이지네이션 및 정렬 정보 (기본 정렬: albumReleaseDate DESC)
     * @return 앨범 목록 페이징 조회 결과
     */
    @Transactional(readOnly = true)
    public Page<AlbumGetAllResponseDto> getAlbumList(Long userId, UserRole role, Pageable pageable) {

        // 삭제되지 않은 유효한 사용자 여부 검증
        userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_CERTIFICATION_REQUIRED));

        // 관리자(ADMIN) 권한 여부 검증
        if (role != UserRole.ADMIN) {
            throw new CustomException(ErrorCode.AUTH_AUTHORIZATION_REQUIRED);
        }

        // 삭제되지 않은 앨범 목록을 페이징 조건에 맞게 조회
        Page<Album> albumPage = albumRepository.findAll(pageable);

        return albumPage.map(AlbumGetAllResponseDto::from);
    }

    /**
     * 앨범 기본 정보 수정 기능 (관리자 전용)
     * @param userId 사용자 ID (JWT 적용 전까지 임시 사용)
     * @param role 사용자 권한
     * @param albumId 수정할 앨범 ID
     * @param requestDto 앨범 수정 요청 DTO
     * @return 수정된 앨범 정보
     */
    @Transactional
    public AlbumUpdateResponseDto updateAlbumInfo(Long userId, UserRole role, Long albumId, AlbumUpdateRequestDto requestDto) {

        // 삭제되지 않은 유효한 사용자 여부 검증
        userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_CERTIFICATION_REQUIRED));

        // 관리자(ADMIN) 권한 여부 검증
        if (role != UserRole.ADMIN) {
            throw new CustomException(ErrorCode.AUTH_AUTHORIZATION_REQUIRED);
        }

        // 변경 필드가 하나도 없을 경우 400 반환
        String albumName = requestDto.getAlbumName();
        LocalDate albumReleaseDate = requestDto.getAlbumReleaseDate();
        String albumImage = requestDto.getAlbumImage();

        if (albumName == null && albumReleaseDate == null && albumImage == null) {
            throw new CustomException(ErrorCode.ALBUM_UPDATE_NO_CHANGES);
        }

        // 수정 대상 앨범 조회 (삭제된 앨범은 수정 불가)
        Album foundAlbum = albumRepository.findByAlbumIdAndIsDeletedFalse(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        // 요청에 포함된 필드만 검증 후 반영
        if (albumName != null) {
            if (albumName.isBlank()) {
                throw new CustomException(ErrorCode.ALBUM_NAME_REQUIRED);
            }
            foundAlbum.updateAlbumName(albumName.trim());
        }

        if (albumReleaseDate != null) {
            foundAlbum.updateAlbumReleaseDate(albumReleaseDate);
        }

        if (albumImage != null) {
            foundAlbum.updateAlbumImage(albumImage);
        }

        // 앨범 이름 또는 발매일 변경 시, 최종 값 기준 중복 여부 검증
        if (albumName != null || albumReleaseDate != null) {
            if (albumRepository.existsByAlbumNameAndAlbumReleaseDateAndIsDeletedFalseAndAlbumIdNot(
                    foundAlbum.getAlbumName(),
                    foundAlbum.getAlbumReleaseDate(),
                    albumId
            )) {
                throw new CustomException(ErrorCode.ALBUM_EXIST_NAME_RELEASE_DATE);
            }
        }

        // 응답 포맷 통일을 위해 현재 참여 아티스트 목록 조회
        //    (아티스트 갱신은 별도 API에서 처리)
        List<ArtistAlbum> mappings = artistAlbumRepository.findAllByAlbum_AlbumId(albumId);
        List<ArtistSummaryDto> artistList = mappings.stream()
                .map(m -> new ArtistSummaryDto(
                        m.getArtist().getArtistId(),
                        m.getArtist().getArtistName()
                ))
                .toList();

        return AlbumUpdateResponseDto.from(foundAlbum, artistList);
    }

    /**
     * 참여 아티스트 목록 전체 갱신 기능 (관리자 전용)
     * @param userId 사용자 ID (JWT 적용 전까지 임시 사용)
     * @param role 사용자 권한
     * @param albumId 갱신할 앨범 ID
     * @param requestDto 참여 아티스트 수정 요청 DTO
     * @return 참여 아티스트가 반영된 앨범 정보
     */
    @Transactional
    public ArtistAlbumUpdateResponseDto updateAlbumArtistList(Long userId, UserRole role, Long albumId, ArtistAlbumUpdateRequestDto requestDto) {

        // 삭제되지 않은 유효한 사용자 여부 검증
        userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_CERTIFICATION_REQUIRED));

        // 관리자(ADMIN) 권한 여부 검증
        if (role != UserRole.ADMIN) {
            throw new CustomException(ErrorCode.AUTH_AUTHORIZATION_REQUIRED);
        }

        // 수정 대상 앨범 조회 (삭제된 앨범은 수정 불가)
        Album foundAlbum = albumRepository.findByAlbumIdAndIsDeletedFalse(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        // 아티스트 조회 (중복 id 방어)
        List<Long> artistIds = requestDto.getArtistIds().stream().distinct().toList();

        List<Artist> artistList = artistRepository.findAllByArtistIdInAndIsDeletedFalse(artistIds);

        // 요청한 아티스트 ID 중 존재하지 않는 항목이 있는지 검증
        if (artistList.size() != artistIds.size()) {
            throw new CustomException(ErrorCode.ARTIST_NOT_FOUND);
        }

        // 앨범 정책에 따라 기존 매핑은 하드 딜리트 후 재생성
        artistAlbumRepository.deleteAllByAlbumId(foundAlbum.getAlbumId());

        // 새 매핑 생성
        List<ArtistAlbum> mappings = artistList.stream()
                .map(artist -> new ArtistAlbum(artist, foundAlbum))
                .toList();
        artistAlbumRepository.saveAll(mappings);

        // 응답에 필요한 아티스트 정보만 DTO로 변환
        List<ArtistSummaryDto> dtoList = artistList.stream()
                .map(artist -> new ArtistSummaryDto(artist.getArtistId(), artist.getArtistName()))
                .toList();

        return ArtistAlbumUpdateResponseDto.from(foundAlbum, dtoList);
    }

    /**
     * 앨범 비활성화 기능 (관리자 전용)
     * @param userId 사용자 ID (JWT 적용 전까지 임시 사용)
     * @param role 사용자 권한
     * @param albumId 비활성화할 앨범 ID
     */
    @Transactional
    public void deleteAlbum(Long userId, UserRole role, Long albumId) {

        // 삭제되지 않은 유효한 사용자 여부 검증
        userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_CERTIFICATION_REQUIRED));

        // 관리자(ADMIN) 권한 여부 검증
        if (role != UserRole.ADMIN) {
            throw new CustomException(ErrorCode.AUTH_AUTHORIZATION_REQUIRED);
        }

        // 비활성화 대상 앨범 조회 (이미 비활성화된 앨범은 제외)
        Album foundAlbum = albumRepository.findByAlbumIdAndIsDeletedFalse(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        // 앨범에 포함된 음원 비활성화
        List<Song> foundSongList = songRepository.findAllByAlbum_AlbumIdAndIsDeletedFalseOrderByPositionAsc(albumId);

        foundSongList.forEach(Song::deleteSong);

        // 앨범 비활성화
        foundAlbum.delete();
    }

    /**
     * 앨범 활성화 기능 (관리자 전용)
     * @param userId 사용자 ID (JWT 적용 전까지 임시 사용)
     * @param role 사용자 권한
     * @param albumId 활성화할 앨범 ID
     */
    @Transactional
    public void restoreAlbum(Long userId, UserRole role, Long albumId) {

        // 삭제되지 않은 유효한 사용자 여부 검증
        userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_CERTIFICATION_REQUIRED));

        // 관리자(ADMIN) 권한 여부 검증
        if (role != UserRole.ADMIN) {
            throw new CustomException(ErrorCode.AUTH_AUTHORIZATION_REQUIRED);
        }

        // 활성화 대상 앨범 조회 (삭제된 앨범만 복구 가능)
        Album foundAlbum = albumRepository.findByAlbumIdAndIsDeletedTrue(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        // 앨범에 포함된 음원 활성화
        List<Song> foundSongList = songRepository.findAllByAlbum_AlbumIdAndIsDeletedTrueOrderByPositionAsc(albumId);

        foundSongList.forEach(Song::restoreSong);

        // 앨범 활성화
        foundAlbum.restore();
    }
}
