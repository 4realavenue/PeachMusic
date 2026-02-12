package com.example.peachmusic.domain.artist.service;

import com.example.peachmusic.common.enums.FileType;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.model.CursorParam;
import com.example.peachmusic.common.model.NextCursor;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.common.storage.FileStorageService;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.artist.dto.response.ArtistImageUpdateResponseDto;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.dto.request.ArtistCreateRequestDto;
import com.example.peachmusic.domain.artist.dto.request.ArtistUpdateRequestDto;
import com.example.peachmusic.domain.artist.dto.response.ArtistCreateResponseDto;
import com.example.peachmusic.domain.artist.dto.response.ArtistSearchResponseDto;
import com.example.peachmusic.domain.artist.dto.response.ArtistUpdateResponseDto;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.artistalbum.repository.ArtistAlbumRepository;
import com.example.peachmusic.domain.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import static com.example.peachmusic.common.constants.SearchViewSize.DETAIL_SIZE;
import static com.example.peachmusic.common.constants.UserViewScope.ADMIN_VIEW;

@Service
@RequiredArgsConstructor
public class ArtistAdminService {

    private final ArtistRepository artistRepository;
    private final SongRepository songRepository;
    private final ArtistAlbumRepository artistAlbumRepository;
    private final FileStorageService fileStorageService;
    private final AlbumRepository albumRepository;

    /**
     * 아티스트 생성 기능 (관리자 전용)
     * @param requestDto   아티스트 생성 요청 DTO
     * @param profileImage 업로드할 아티스트 프로필 이미지 파일 (선택)
     * @return 생성된 아티스트 정보
     */
    @Transactional
    public ArtistCreateResponseDto createArtist(ArtistCreateRequestDto requestDto, MultipartFile profileImage) {

        String artistName = requestDto.getArtistName().trim();
        String country = normalize(requestDto.getCountry());
        String bio = normalize(requestDto.getBio());

        String storedPath = "https://img.peachmusics.com/storage/image/default-image.jpg";

        Artist artist = new Artist(artistName, storedPath, country, requestDto.getArtistType(), requestDto.getDebutDate(), bio);
        Artist savedArtist = artistRepository.save(artist);

        if (profileImage != null && !profileImage.isEmpty()) {
            storedPath = storeProfileImage(profileImage, savedArtist.getArtistId());

            savedArtist.updateProfileImage(storedPath);
        }

        return ArtistCreateResponseDto.from(savedArtist);
    }

    /**
     * 전체 아티스트 조회 기능 (관리자 전용)
     * @return 아티스트 목록 페이징 조회 결과
     */
    @Transactional(readOnly = true)
    public KeysetResponse<ArtistSearchResponseDto> getArtistList(String word, CursorParam cursor) {

        final int size = DETAIL_SIZE;

        List<ArtistSearchResponseDto> content = artistRepository.findArtistKeysetPageByWord(word, size, ADMIN_VIEW, null, null, cursor);

        return KeysetResponse.of(content, size, last -> new NextCursor(last.getArtistId(), null));
    }

    /**
     * 아티스트 기본 정보 수정 기능 (관리자 전용)
     * @param artistId 수정할 아티스트 ID
     * @param requestDto 아티스트 기본 정보 수정 요청 DTO
     * @return 수정된 아티스트 정보
     */
    @Transactional
    public ArtistUpdateResponseDto updateArtist(Long artistId, ArtistUpdateRequestDto requestDto) {

        Artist foundArtist = getArtistOrThrow(artistId, false, ErrorCode.ARTIST_NOT_FOUND);

        foundArtist.updateArtistInfo(requestDto);

        return ArtistUpdateResponseDto.from(foundArtist);
    }

    /**
     * 아티스트 프로필 이미지 수정 기능 (관리자 전용)
     * @param artistId 프로필 이미지를 수정할 아티스트 ID
     * @param profileImage 업로드할 새로운 프로필 이미지 파일
     * @return 수정된 프로필 이미지가 반영된 아티스트 정보
     */
    @Transactional
    public ArtistImageUpdateResponseDto updateProfileImage(Long artistId, MultipartFile profileImage) {

        Artist foundArtist = getArtistOrThrow(artistId, false, ErrorCode.ARTIST_NOT_FOUND);

        String oldPath = foundArtist.getProfileImage();

        String newPath = storeProfileImage(profileImage, foundArtist.getArtistId());

        foundArtist.updateProfileImage(newPath);

        if (oldPath != null) {
            fileStorageService.deleteFileByPath(oldPath);
        }

        return ArtistImageUpdateResponseDto.from(foundArtist);
    }

    /**
     * 아티스트 비활성화 기능 (관리자 전용)
     * @param artistId 비활성화할 아티스트 ID
     */
    @Transactional
    public void deleteArtist(Long artistId) {

        Artist foundArtist = getArtistOrThrow(artistId, false, ErrorCode.ARTIST_DETAIL_NOT_FOUND);

        foundArtist.delete();

        List<Long> albumIdList = artistAlbumRepository.findAlbumIdListByArtistIdAndIsDeleted(artistId, false);

        if (!albumIdList.isEmpty()) {
            songRepository.softDeleteByAlbumIdList(albumIdList);
            albumRepository.softDeleteByAlbumIdList(albumIdList);
        }
    }

    /**
     * 아티스트 활성화 기능 (관리자 전용)
     * @param artistId 활성화할 아티스트 ID
     */
    @Transactional
    public void restoreArtist(Long artistId) {

        Artist foundArtist = getArtistOrThrow(artistId, true, ErrorCode.ARTIST_DETAIL_NOT_FOUND);

        foundArtist.restore();

        List<Long> albumIdList = artistAlbumRepository.findAlbumIdListByArtistIdAndIsDeleted(artistId, true);

        if (!albumIdList.isEmpty()) {
            songRepository.restoreByAlbumIdList(albumIdList);
            albumRepository.restoreByAlbumIdList(albumIdList);
        }
    }

    private Artist getArtistOrThrow(Long artistId, boolean isDeleted, ErrorCode errorCode) {
        return artistRepository.findByArtistIdAndIsDeleted(artistId, isDeleted)
                .orElseThrow(() -> new CustomException(errorCode));
    }

    private String normalize(String value) {
        return (value != null && !value.isBlank()) ? value.trim() : null;
    }

    private String storeProfileImage(MultipartFile profileImage, Long artistId) {
        String baseName = artistId.toString();
        return fileStorageService.storeFile(profileImage, FileType.ARTIST_PROFILE, baseName);
    }
}
