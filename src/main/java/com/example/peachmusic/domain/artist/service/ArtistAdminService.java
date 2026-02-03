package com.example.peachmusic.domain.artist.service;

import com.example.peachmusic.common.enums.FileType;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.model.Cursor;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.common.service.AbstractKeysetService;
import com.example.peachmusic.common.storage.FileStorageService;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.artist.dto.response.ArtistImageUpdateResponseDto;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.dto.request.ArtistCreateRequestDto;
import com.example.peachmusic.domain.artist.dto.request.ArtistUpdateRequestDto;
import com.example.peachmusic.domain.artist.dto.response.ArtistCreateResponseDto;
import com.example.peachmusic.domain.artist.dto.response.ArtistSearchResponseDto;
import com.example.peachmusic.domain.artist.dto.response.ArtistUpdateResponseDto;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.artistalbum.repository.ArtistAlbumRepository;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtistAdminService extends AbstractKeysetService {

    private final ArtistRepository artistRepository;
    private final SongRepository songRepository;
    private final ArtistAlbumRepository artistAlbumRepository;
    private final FileStorageService fileStorageService;

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

        String storedPath = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            storedPath = storeProfileImage(profileImage, artistName);
        }

        Artist artist = new Artist(artistName, storedPath, country, requestDto.getArtistType(), requestDto.getDebutDate(), bio);
        Artist savedArtist = artistRepository.save(artist);

        return ArtistCreateResponseDto.from(savedArtist);
    }

    /**
     * 전체 아티스트 조회 기능 (관리자 전용)
     * @return 아티스트 목록 페이징 조회 결과
     */
    @Transactional(readOnly = true)
    public KeysetResponse<ArtistSearchResponseDto> getArtistList(String word, Long lastId) {

        validateWord(word); // 단어 검증

        String[] words = word.split("\\s+");
        final int size = 10;
        final boolean isAdmin = true; // 관리자용

        List<ArtistSearchResponseDto> content = artistRepository.findArtistKeysetPageByWord(words, size, isAdmin, null, null, lastId, null, null);

        return toKeysetResponse(content, size, last -> new Cursor(last.getArtistId(), null));
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

        if (!hasUpdateFields(requestDto)) {
            throw new CustomException(ErrorCode.ARTIST_UPDATE_NO_CHANGES);
        }

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

        String newPath = storeProfileImage(profileImage, foundArtist.getArtistName());

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

        Artist foundArtist = getArtistOrThrow(artistId, true, ErrorCode.ARTIST_DETAIL_NOT_FOUND);

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

    private Artist getArtistOrThrow(Long artistId, boolean isDeleted, ErrorCode errorCode) {
        return artistRepository.findByArtistIdAndIsDeleted(artistId, isDeleted)
                .orElseThrow(() -> new CustomException(errorCode));
    }

    private String normalize(String value) {
        return (value != null && !value.isBlank()) ? value.trim() : null;
    }

    private boolean hasUpdateFields(ArtistUpdateRequestDto requestDto) {
        return (requestDto.getArtistName() != null && !requestDto.getArtistName().isBlank())
                || (requestDto.getCountry() != null && !requestDto.getCountry().isBlank())
                || requestDto.getArtistType() != null
                || requestDto.getDebutDate() != null
                || (requestDto.getBio() != null && !requestDto.getBio().isBlank());
    }

    private String storeProfileImage(MultipartFile profileImage, String artistName) {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String baseName = "PeachMusic_artist_" + artistName + "_" + date;
        return fileStorageService.storeFile(profileImage, FileType.ARTIST_PROFILE, baseName);
    }
}
