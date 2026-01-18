package com.example.peachmusic.domain.album.service;

import com.example.peachmusic.common.enums.UserRole;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.exception.ErrorCode;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.model.request.AlbumCreateRequestDto;
import com.example.peachmusic.domain.album.model.response.AlbumCreateResponseDto;
import com.example.peachmusic.domain.album.model.response.ArtistSummaryDto;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.artistAlbum.entity.ArtistAlbum;
import com.example.peachmusic.domain.artistAlbum.repository.ArtistAlbumRepository;
import com.example.peachmusic.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

        // 이미 활성 상태로 존재하는 앨범인지 확인
        boolean exists = albumRepository.existsByAlbumNameAndAlbumReleaseDateAndIsDeletedFalse(albumName, albumReleaseDate);
        if (exists) {
            throw new CustomException(ErrorCode.ALBUM_EXIST_NAME_RELEASE_DATE);
        }

        // 동일한 앨범이 비활성 상태로 존재하는지 확인 (복구 유도)
        Optional<Album> deleted = albumRepository.findByAlbumNameAndAlbumReleaseDateAndIsDeletedTrue(albumName, albumReleaseDate);
        if (deleted.isPresent()) {
            throw new CustomException(ErrorCode.ALBUM_EXIST_NAME_RELEASE_DATE_DELETED);
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
}
