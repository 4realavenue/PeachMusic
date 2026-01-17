package com.example.peachmusic.domain.artist.service;

import com.example.peachmusic.common.enums.UserRole;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.exception.ErrorCode;
import com.example.peachmusic.common.model.PageResponse;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.model.request.ArtistCreateRequestDto;
import com.example.peachmusic.domain.artist.model.request.ArtistUpdateRequestDto;
import com.example.peachmusic.domain.artist.model.response.ArtistCreateResponseDto;
import com.example.peachmusic.domain.artist.model.response.ArtistGetAllResponseDto;
import com.example.peachmusic.domain.artist.model.response.ArtistUpdateResponseDto;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArtistAdminService {

    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;

    /**
     * 아티스트 생성 기능 (관리자 전용)
     * JWT 적용 전 단계로, 사용자 식별 정보와 권한을 파라미터로 임시 전달받아 처리한다.
     *
     * @param userId 사용자 ID (JWT 적용 전까지 임시 사용)
     * @param role 사용자 권한
     * @param requestDto 아티스트 생성 요청 DTO
     * @return 생성된 아티스트 정보
     */
    @Transactional
    public ArtistCreateResponseDto createArtist(Long userId, UserRole role, ArtistCreateRequestDto requestDto) {

        // 삭제되지 않은 유효한 사용자 여부 검증
        userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_CERTIFICATION_REQUIRED));

        // 관리자(ADMIN) 권한 여부 검증
        if (role != UserRole.ADMIN) {
            throw new CustomException(ErrorCode.AUTH_AUTHORIZATION_REQUIRED);
        }

        String artistName = requestDto.getArtistName();

        // 이미 활성 상태로 존재하는 아티스트 이름인지 확인
        boolean exists = artistRepository.existsByArtistNameAndIsDeletedFalse(artistName);
        if (exists) {
            throw new CustomException(ErrorCode.DUPLICATE_ARTIST);
        }

        // 동일한 아티스트 이름이 비활성 상태로 존재하는지 확인 (복구 유도)
        Optional<Artist> deleted = artistRepository.findByArtistNameAndIsDeletedTrue(artistName);
        if (deleted.isPresent()) {
            throw new CustomException(ErrorCode.ARTIST_DELETED_ALREADY);
        }

        // 요청 값으로 아티스트 엔티티 생성 및 저장
        Artist artist = new Artist(artistName);
        Artist savedArtist = artistRepository.save(artist);

        return ArtistCreateResponseDto.from(savedArtist);
    }

    /**
     * 전체 아티스트 조회 기능 (관리자 전용)
     * @param userId 사용자 ID (JWT 적용 전까지 임시 사용)
     * @param role 사용자 권한
     * @param pageable 페이지네이션 및 정렬 정보 (기본 정렬: createdAt DESC)
     * @return 아티스트 목록 페이징 조회 결과
     */
    @Transactional(readOnly = true)
    public PageResponse<ArtistGetAllResponseDto> getArtistList(Long userId, UserRole role, Pageable pageable) {

        // 삭제되지 않은 유효한 사용자 여부 검증
        userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_CERTIFICATION_REQUIRED));

        // 관리자(ADMIN) 권한 여부 검증
        if (role != UserRole.ADMIN) {
            throw new CustomException(ErrorCode.AUTH_AUTHORIZATION_REQUIRED);
        }

        // 삭제되지 않은 아티스트 목록을 페이징 조건에 맞게 조회
        Page<Artist> artistPage = artistRepository.findAllByIsDeletedFalse(pageable);

        // Page<Artist>를 응답 DTO 페이지로 변환
        Page<ArtistGetAllResponseDto> dtoPage = artistPage.map(ArtistGetAllResponseDto::from);

        return PageResponse.success("아티스트 목록 조회 성공", dtoPage);
    }

    /**
     * 아티스트 수정 기능 (관리자 전용)
     * @param userId 사용자 ID (JWT 적용 전까지 임시 사용)
     * @param role 사용자 권한
     * @param artistId 수정할 아티스트 ID
     * @param requestDto 아티스트 수정 요청 DTO
     * @return 수정된 아티스트 정보
     */
    @Transactional
    public ArtistUpdateResponseDto updateArtist(Long userId, UserRole role, Long artistId, ArtistUpdateRequestDto requestDto) {

        // 삭제되지 않은 유효한 사용자 여부 검증
        userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_CERTIFICATION_REQUIRED));

        // 관리자(ADMIN) 권한 여부 검증
        if (role != UserRole.ADMIN) {
            throw new CustomException(ErrorCode.AUTH_AUTHORIZATION_REQUIRED);
        }

        // 수정 대상 아티스트 조회 (삭제되지 않은 아티스트만 허용)
        Artist foundArtist = artistRepository.findByArtistIdAndIsDeletedFalse(artistId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTIST_NOT_FOUND));

        String newName = requestDto.getArtistName();

        // 변경이 없는 경우(같은 이름으로 수정 요청)면 그대로 반환
        if (foundArtist.getArtistName().equals(newName)) {
            return ArtistUpdateResponseDto.from(foundArtist);
        }

        // 다른 아티스트가 동일 이름을 사용 중인지 검증
        boolean exists = artistRepository.existsByArtistNameAndIsDeletedFalseAndArtistIdNot(newName, artistId);
        if (exists) {
            throw new CustomException(ErrorCode.ARTIST_NAME_DUPLICATED);
        }

        // 아티스트 이름 변경
        foundArtist.updateArtistName(newName);

        return ArtistUpdateResponseDto.from(foundArtist);
    }
}
