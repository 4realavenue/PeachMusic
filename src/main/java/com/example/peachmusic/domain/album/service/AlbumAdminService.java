package com.example.peachmusic.domain.album.service;

import com.example.peachmusic.common.enums.FileType;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.model.NextCursor;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.common.storage.FileStorageService;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.dto.request.AlbumCreateRequestDto;
import com.example.peachmusic.domain.album.dto.request.AlbumUpdateRequestDto;
import com.example.peachmusic.domain.album.dto.request.ArtistAlbumUpdateRequestDto;
import com.example.peachmusic.domain.album.dto.response.*;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.artistalbum.entity.ArtistAlbum;
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
import static com.example.peachmusic.common.constants.SearchViewSize.DETAIL_SIZE;
import static com.example.peachmusic.common.constants.UserViewScope.ADMIN_VIEW;

@Service
@RequiredArgsConstructor
public class AlbumAdminService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final ArtistAlbumRepository artistAlbumRepository;
    private final SongRepository songRepository;
    private final FileStorageService fileStorageService;

    /**
     * 앨범 생성 기능 (관리자 전용)
     *
     * @param requestDto 앨범 생성 요청 DTO
     * @param albumImage 업로드할 앨범 이미지 파일
     * @return 생성된 앨범 정보
     */
    @Transactional
    public AlbumCreateResponseDto createAlbum(AlbumCreateRequestDto requestDto, MultipartFile albumImage) {

        List<Long> artistIdList = distinctArtistIdList(requestDto.getArtistIdList());
        List<Artist> artistList = getActiveArtistListOrThrow(artistIdList);

        String albumName = requestDto.getAlbumName().trim();
        LocalDate albumReleaseDate = requestDto.getAlbumReleaseDate();

        albumRepository.findByAlbumNameAndAlbumReleaseDate(albumName, albumReleaseDate)
            .ifPresent(album -> {
                if (album.isDeleted()) {
                    throw new CustomException(ErrorCode.ALBUM_EXIST_NAME_RELEASE_DATE_DELETED);
            }
            throw new CustomException(ErrorCode.ALBUM_EXIST_NAME_RELEASE_DATE);
        });

        String storedPath = storeAlbumImage(albumImage, albumName);

        Album album = new Album(albumName, albumReleaseDate, storedPath);
        Album savedAlbum = albumRepository.save(album);

        // 참여 아티스트와 앨범의 N:M 관계를 매핑 테이블(ArtistAlbum)에 저장
        List<ArtistAlbum> artistAlbumList = artistList.stream()
                .map(artist -> new ArtistAlbum(artist, savedAlbum))
                .toList();
        artistAlbumRepository.saveAll(artistAlbumList);

        List<ArtistSummaryDto> dtoList = toArtistSummaryList(artistAlbumList);

        return AlbumCreateResponseDto.from(savedAlbum, dtoList);
    }

    /**
     * 전체 앨범 조회 기능 (관리자 전용)
     * @return 앨범 목록 페이징 조회 결과
     */
    @Transactional(readOnly = true)
    public KeysetResponse<AlbumSearchResponseDto> getAlbumList(String word, Long lastId) {

        String[] words = word == null ? null : word.split("\\s+");
        final int size = DETAIL_SIZE;

        List<AlbumSearchResponseDto> content = albumRepository.findAlbumKeysetPageByWord(words, size, ADMIN_VIEW, null, null, lastId, null, null, null);

        return KeysetResponse.of(content, size, last -> new NextCursor(last.getAlbumId(), null));
    }

    /**
     * 앨범 기본 정보 수정 기능 (관리자 전용)
     * @param albumId 수정할 앨범 ID
     * @param requestDto 앨범 수정 요청 DTO
     * @return 수정된 앨범 정보
     */
    @Transactional
    public AlbumUpdateResponseDto updateAlbumInfo(Long albumId, AlbumUpdateRequestDto requestDto) {

        Album foundAlbum = getAlbumOrThrow(albumId);

        if (!hasUpdateFields(requestDto)) {
            throw new CustomException(ErrorCode.ALBUM_UPDATE_NO_CHANGES);
        }

        foundAlbum.updateAlbumInfo(requestDto);

        if (albumRepository.existsByAlbumNameAndAlbumReleaseDateAndIsDeletedFalseAndAlbumIdNot(
                   foundAlbum.getAlbumName(), foundAlbum.getAlbumReleaseDate(), albumId)) {
               throw new CustomException(ErrorCode.ALBUM_EXIST_NAME_RELEASE_DATE);
        }

        List<ArtistSummaryDto> artistList = getArtistSummaryListByAlbumId(albumId);

        return AlbumUpdateResponseDto.from(foundAlbum, artistList);
    }

    /**
     * 참여 아티스트 목록 전체 갱신 기능 (관리자 전용)
     * @param albumId 갱신할 앨범 ID
     * @param requestDto 참여 아티스트 수정 요청 DTO
     * @return 참여 아티스트가 반영된 앨범 정보
     */
    @Transactional
    public ArtistAlbumUpdateResponseDto updateAlbumArtistList(Long albumId, ArtistAlbumUpdateRequestDto requestDto) {

        Album foundAlbum = getAlbumOrThrow(albumId);

        List<Long> artistIdList = distinctArtistIdList(requestDto.getArtistIdList());
        List<Artist> artistList = getActiveArtistListOrThrow(artistIdList);

        // 앨범 정책에 따라 기존 매핑은 하드 딜리트 후 재생성
        artistAlbumRepository.deleteAllByAlbumId(foundAlbum.getAlbumId());

        List<ArtistAlbum> artistAlbumList = artistList.stream()
                .map(artist -> new ArtistAlbum(artist, foundAlbum))
                .toList();

        artistAlbumRepository.saveAll(artistAlbumList);

        List<ArtistSummaryDto> dtoList = toArtistSummaryList(artistAlbumList);

        return ArtistAlbumUpdateResponseDto.from(foundAlbum, dtoList);
    }

    /**
     * 앨범 이미지 수정 기능 (관리자 전용)
     * @param albumId 수정할 앨범 ID
     * @param albumImage 업로드할 앨범 이미지 파일
     * @return 이미지가 수정된 앨범 정보
     */
    @Transactional
    public AlbumImageUpdateResponseDto updateAlbumImage(Long albumId, MultipartFile albumImage) {

        Album foundAlbum = getAlbumOrThrow(albumId);
        String oldPath = foundAlbum.getAlbumImage();

        String newPath = storeAlbumImage(albumImage, foundAlbum.getAlbumName());

        foundAlbum.updateAlbumImage(newPath);

        if (oldPath != null && !oldPath.equals(newPath) && isManagedFilePath(oldPath)) {
            fileStorageService.deleteFileByPath(oldPath);
        }

        List<ArtistSummaryDto> artistList = getArtistSummaryListByAlbumId(albumId);

        return AlbumImageUpdateResponseDto.from(foundAlbum, artistList);
    }

    /**
     * 앨범 비활성화 기능 (관리자 전용)
     * @param albumId 비활성화할 앨범 ID
     */
    @Transactional
    public void deleteAlbum(Long albumId) {

        Album foundAlbum = albumRepository.findByAlbumIdAndIsDeletedFalse(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_DETAIL_NOT_FOUND));

        List<Song> foundSongList = songRepository.findAllByAlbum_AlbumIdAndIsDeletedFalse(albumId);

        foundSongList.forEach(Song::deleteSong);

        foundAlbum.delete();
    }

    /**
     * 앨범 활성화 기능 (관리자 전용)
     * @param albumId 활성화할 앨범 ID
     */
    @Transactional
    public void restoreAlbum(Long albumId) {

        Album foundAlbum = albumRepository.findByAlbumIdAndIsDeletedTrue(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_DETAIL_NOT_FOUND));

        List<Song> foundSongList = songRepository.findAllByAlbum_AlbumIdAndIsDeletedTrue(albumId);

        foundSongList.forEach(Song::restoreSong);

        foundAlbum.restore();
    }

    private Album getAlbumOrThrow(Long albumId) {
        return albumRepository.findByAlbumIdAndIsDeletedFalse(albumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));
    }

    private List<Long> distinctArtistIdList(List<Long> artistIdList) {
        return artistIdList.stream().distinct().toList();
    }

    private List<Artist> getActiveArtistListOrThrow(List<Long> artistIdList) {
        List<Artist> artistList = artistRepository.findAllByArtistIdInAndIsDeletedFalse(artistIdList);
        if (artistList.size() != artistIdList.size()) {
            throw new CustomException(ErrorCode.ARTIST_NOT_FOUND);
        }
        return artistList;
    }

    private List<ArtistSummaryDto> toArtistSummaryList(List<ArtistAlbum> artistAlbumList) {
        return artistAlbumList.stream()
                .map(artist -> ArtistSummaryDto.from(artist.getArtist()))
                .toList();
    }

    private List<ArtistSummaryDto> getArtistSummaryListByAlbumId(Long albumId) {
        return artistAlbumRepository.findAllByAlbum_AlbumId(albumId).stream()
                .map(artist -> ArtistSummaryDto.from(artist.getArtist()))
                .toList();
    }

    private boolean hasUpdateFields(AlbumUpdateRequestDto requestDto) {
        return (requestDto.getAlbumName() != null && !requestDto.getAlbumName().isBlank())
                || requestDto.getAlbumReleaseDate() != null;
    }

    private String storeAlbumImage(MultipartFile albumImage, String albumName) {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String baseName = "PeachMusic_album_" + albumName + "_" + date;
        return fileStorageService.storeFile(albumImage, FileType.ALBUM_IMAGE, baseName);
    }

    private boolean isManagedFilePath(String path) {
        return !path.startsWith("https://");
    }
}
