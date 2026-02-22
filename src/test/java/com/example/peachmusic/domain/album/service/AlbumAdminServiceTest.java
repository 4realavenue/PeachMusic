package com.example.peachmusic.domain.album.service;

import com.example.peachmusic.common.enums.ArtistType;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.enums.FileType;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.storage.FileStorageService;
import com.example.peachmusic.domain.album.dto.request.AlbumCreateRequestDto;
import com.example.peachmusic.domain.album.dto.request.AlbumUpdateRequestDto;
import com.example.peachmusic.domain.album.dto.request.ArtistAlbumUpdateRequestDto;
import com.example.peachmusic.domain.album.dto.response.AlbumCreateResponseDto;
import com.example.peachmusic.domain.album.dto.response.AlbumUpdateResponseDto;
import com.example.peachmusic.domain.album.dto.response.ArtistAlbumUpdateResponseDto;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.artistalbum.entity.ArtistAlbum;
import com.example.peachmusic.domain.artistalbum.repository.ArtistAlbumRepository;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.repository.SongRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@ActiveProfiles("test")
@Transactional
class AlbumAdminServiceTest {

    @Autowired
    private AlbumAdminService albumAdminService;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private ArtistAlbumRepository artistAlbumRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    private FileStorageService fileStorageService;

    private Artist savedArtist(String name) {
        return artistRepository.save(new Artist(
                name,
                "https://image.test/" + UUID.randomUUID(),
                "대한민국",
                ArtistType.SOLO,
                LocalDate.of(2024, 1, 1),
                "안녕하세요."
        ));
    }

    private Album savedAlbum(String name, LocalDate date) {
        Album album = new Album(name, date);

        album.updateAlbumImage("https://img.peachmusics.com/storage/image/default-image-" + UUID.randomUUID() + ".jpg");

        return albumRepository.save(album);
    }

    private Song savedSong(Album album, String name) {
        return songRepository.save(new Song(
                album,
                name,
                180L,
                "https://license.test",
                1L,
                "https://audio.test" + UUID.randomUUID(),
                "vocal",
                "en",
                "medium",
                "guitar",
                "test"
                )
        );
    }

    private void mapArtistToAlbum(Artist artist, Album album) {
        artistAlbumRepository.save(new ArtistAlbum(artist, album));
    }

    private AlbumCreateRequestDto requestDto(String albumName, LocalDate date, List<Long> artistIdList) throws Exception {
        String json = """
                {
                    "albumName": "%s",
                    "albumReleaseDate": "%s",
                    "artistIdList": %s
                }
                """.formatted(albumName, date, objectMapper.writeValueAsString(artistIdList));
        return objectMapper.readValue(json, AlbumCreateRequestDto.class);
    }

    private AlbumUpdateRequestDto updateRequestDto(String name, LocalDate date) throws Exception {
        String json = """
                {
                    "albumName": "%s",
                    "albumReleaseDate": "%s"
                }
                """.formatted(name, date);
        return objectMapper.readValue(json, AlbumUpdateRequestDto.class);
    }

    private ArtistAlbumUpdateRequestDto requestDto(List<Long> artistIdList) throws Exception {
        String json = """
                {
                    "artistIdList": %s
                }
                """.formatted(objectMapper.writeValueAsString(artistIdList));
        return objectMapper.readValue(json, ArtistAlbumUpdateRequestDto.class);
    }

    @BeforeEach
    void setupStorageMock() {
        Mockito.when(fileStorageService.storeFile(
                Mockito.any(MultipartFile.class),
                Mockito.any(FileType.class),
                Mockito.anyString()
        )).thenReturn("albums/test/album.png");
    }

    @Test
    @DisplayName("앨범 생성 성공")
    void createAlbum_success() throws Exception {
        // given
        Artist artistA = savedArtist("아티스트A");
        Artist artistB = savedArtist("아티스트B");

        String albumName = "테스트 앨범-" + UUID.randomUUID();

        java.util.List<Long> artistIdList = java.util.List.of(artistA.getArtistId(), artistA.getArtistId(), artistB.getArtistId());

        AlbumCreateRequestDto request = requestDto(albumName, LocalDate.of(2024, 1, 1), artistIdList);

        MockMultipartFile albumImage = new MockMultipartFile("albumImage", "album.png", "image/png", "dummy".getBytes());

        // when
        AlbumCreateResponseDto responseDto = albumAdminService.createAlbum(request, albumImage);

        // then - 응답 검증
        assertEquals(albumName, responseDto.getAlbumName());
        assertEquals(2, responseDto.getArtistList().size());

        // then - 앨범 저장 검증
        Album savedAlbum = albumRepository.findByAlbumIdAndIsDeletedFalse(responseDto.getAlbumId()).orElseThrow();

        assertEquals(albumName, savedAlbum.getAlbumName());
        assertEquals(LocalDate.of(2024, 1, 1), savedAlbum.getAlbumReleaseDate());
        assertEquals("albums/test/album.png", savedAlbum.getAlbumImage());

        // then - N:M 매핑 검증
        List<ArtistAlbum> mappings = artistAlbumRepository.findAllByAlbum_AlbumId(savedAlbum.getAlbumId());

        assertEquals(2, mappings.size());
    }

    @Test
    @DisplayName("앨범명과 발매일이 이미 존재하면 예외 발생")
    void createAlbum_duplicate_notDeleted() throws Exception {
        // given
        Artist artist = savedArtist("아티스트");

        String albumName = "테스트 앨범-" + UUID.randomUUID();

        albumRepository.save(new Album(albumName, LocalDate.of(2024, 1, 1)));

        java.util.List<Long> artistIdList = java.util.List.of(artist.getArtistId());

        AlbumCreateRequestDto request = requestDto(albumName, LocalDate.of(2024, 1, 1), artistIdList);

        MockMultipartFile albumImage = new MockMultipartFile("albumImage", "album.png", "image/png", "dummy".getBytes());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> albumAdminService.createAlbum(request, albumImage));

        // then
        assertEquals(ErrorCode.ALBUM_EXIST_NAME_RELEASE_DATE, exception.getErrorCode());
    }

    @Test
    @DisplayName("삭제된 앨범과 앨범명/발매일이 같으면 삭제 중복 예외 발생")
    void createAlbum_duplicate_deleted() throws Exception {
        // given
        Artist artist = savedArtist("아티스트");

        String albumName = "테스트 앨범-" + UUID.randomUUID();

        Album deletedAlbum = albumRepository.save(new Album(albumName, LocalDate.of(2024, 1, 1)));

        deletedAlbum.delete();

        java.util.List<Long> artistIdList = java.util.List.of(artist.getArtistId());

        AlbumCreateRequestDto request = requestDto(albumName, LocalDate.of(2024, 1, 1), artistIdList);

        MockMultipartFile albumImage = new MockMultipartFile("albumImage", "album.png", "image/png", "dummy".getBytes());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> albumAdminService.createAlbum(request, albumImage));

        // then
        assertEquals(ErrorCode.ALBUM_EXIST_NAME_RELEASE_DATE_DELETED, exception.getErrorCode());
    }

    @Test
    @DisplayName("앨범 기본 정보 수정 성공")
    void updateAlbumInfo_success() throws Exception {
        // given
        Artist artist = savedArtist("아티스트A");

        Album album = savedAlbum("원래 앨범", LocalDate.of(2024, 1, 1));
        mapArtistToAlbum(artist, album);

        AlbumUpdateRequestDto requestDto = updateRequestDto("수정 앨범명-" + UUID.randomUUID(), LocalDate.of(2024, 2, 1));

        // when
        AlbumUpdateResponseDto responseDto = albumAdminService.updateAlbumInfo(album.getAlbumId(), requestDto);

        // then - 응답
        assertEquals(requestDto.getAlbumName().trim(), responseDto.getAlbumName());
        assertEquals(requestDto.getAlbumReleaseDate(), responseDto.getAlbumReleaseDate());

        // then - 영속 상태 확인
        Album updated = albumRepository.findByAlbumIdAndIsDeletedFalse(album.getAlbumId()).orElseThrow();
        assertEquals(requestDto.getAlbumName().trim(), updated.getAlbumName());
        assertEquals(requestDto.getAlbumReleaseDate(), updated.getAlbumReleaseDate());

        // 참여 아티스트 리스트도 응답에 포함되는지(최소 1개)
        assertTrue(responseDto.getArtistList().size() >= 1);
    }

    @Test
    @DisplayName("수정 결과가 다른 앨범과 앨범명+발매일 중복이면 예외 발생")
    void updateAlbumInfo_duplicate_throws() throws Exception {
        // given
        Album albumA = savedAlbum("앨범A-" + UUID.randomUUID(), LocalDate.of(2024, 1, 1));
        Album albumB = savedAlbum("앨범B-" + UUID.randomUUID(), LocalDate.of(2024, 2, 1));

        // albumB의 값으로 albumA를 바꾸려는 상황(=중복)
        AlbumUpdateRequestDto requestDto = updateRequestDto(albumB.getAlbumName(), albumB.getAlbumReleaseDate());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> albumAdminService.updateAlbumInfo(albumA.getAlbumId(), requestDto));

        // then
        assertEquals(ErrorCode.ALBUM_EXIST_NAME_RELEASE_DATE, exception.getErrorCode());
    }

    @Test
    @DisplayName("존재하지 않는 albumId면 예외 발생")
    void updateAlbumInfo_albumNotFound_throws() throws Exception {
        // given
        AlbumUpdateRequestDto requestDto = updateRequestDto("아무거나-" + UUID.randomUUID(), LocalDate.of(2024, 3, 1));

        // when
        CustomException exception = assertThrows(CustomException.class, () -> albumAdminService.updateAlbumInfo(999999L, requestDto));

        // then
        assertEquals(ErrorCode.ALBUM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("참여 아티스트 목록 갱신 성공 - 기존 매핑은 삭제되고 요청 목록으로 재생성(중복 제거)")
    void updateAlbumArtistList_success() throws Exception {
        // given
        Album album = savedAlbum("앨범-" + UUID.randomUUID(), LocalDate.of(2024, 1, 1));

        Artist old1 = savedArtist("기존1");
        Artist old2 = savedArtist("기존2");
        mapArtistToAlbum(old1, album);
        mapArtistToAlbum(old2, album);

        Artist new1 = savedArtist("신규1");
        Artist new2 = savedArtist("신규2");

        // 중복 포함 요청
        ArtistAlbumUpdateRequestDto requestDto = requestDto(List.of(new1.getArtistId(), new1.getArtistId(), new2.getArtistId()));

        // when
        ArtistAlbumUpdateResponseDto responseDto = albumAdminService.updateAlbumArtistList(album.getAlbumId(), requestDto);

        // then - 응답에 참여 아티스트가 2명으로 반영(중복 제거)
        assertEquals(2, responseDto.getArtistList().size());

        // then - DB 매핑이 기존 2명 -> 신규 2명으로 갈아껴졌는지 확인
        List<ArtistAlbum> mappings = artistAlbumRepository.findAllByAlbum_AlbumId(album.getAlbumId());

        assertEquals(2, mappings.size());

        // 신규 아티스트 id만 존재해야 함
        List<Long> mappedArtistIds = mappings.stream()
                .map(m -> m.getArtist().getArtistId())
                .toList();

        assertThat(mappedArtistIds).containsExactlyInAnyOrder(new1.getArtistId(), new2.getArtistId());

        // 기존 아티스트는 더 이상 매핑에 없어야 함
        assertThat(mappedArtistIds).doesNotContain(old1.getArtistId(), old2.getArtistId());
    }

    @Test
    @DisplayName("존재하지 않는 albumId면 예외 발생")
    void updateAlbumArtistList_albumNotFound() throws Exception {
        // given
        Artist a1 = savedArtist("아티스트1");
        ArtistAlbumUpdateRequestDto requestDto = requestDto(List.of(a1.getArtistId()));

        // when
        CustomException exception = assertThrows(CustomException.class, () -> albumAdminService.updateAlbumArtistList(999999L, requestDto));

        // then
        assertEquals(ErrorCode.ALBUM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("비활성/삭제 아티스트가 포함되면 예외 발생")
    void updateAlbumArtistList_inactiveArtist_throws() throws Exception {
        // given
        Album album = savedAlbum("앨범-" + UUID.randomUUID(), LocalDate.of(2024, 1, 1));

        Artist active = savedArtist("활성");
        Artist inactive = savedArtist("비활성");

        inactive.delete();

        ArtistAlbumUpdateRequestDto requestDto = requestDto(List.of(active.getArtistId(), inactive.getArtistId()));

        // when
        CustomException exception = assertThrows(CustomException.class, () -> albumAdminService.updateAlbumArtistList(album.getAlbumId(), requestDto));

        // then
        assertEquals(ErrorCode.ARTIST_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("기존 경로가 관리 파일이면 기존 파일 삭제 호출")
    void updateAlbumImage_deleteOldManagedFile() {
        // given
        String albumName = "앨범-" + UUID.randomUUID();
        String oldPath = "albums/" + albumName + "/old.png";

        Album album = savedAlbum(albumName, LocalDate.of(2024, 1, 1));
        mapArtistToAlbum(savedArtist("아티스트1"), album);

        album.updateAlbumImage(oldPath);
        albumRepository.flush();

        MockMultipartFile newImage = new MockMultipartFile("albumImage", "new.png", "image/png", "dummy".getBytes());

        // when
        albumAdminService.updateAlbumImage(album.getAlbumId(), newImage);

        // then
        Album updated = albumRepository.findById(album.getAlbumId()).orElseThrow();
        assertThat(updated.getAlbumImage()).isEqualTo("albums/test/album.png");

        Mockito.verify(fileStorageService, Mockito.times(1)).deleteFileByPath(oldPath);
    }

    @Test
    @DisplayName("기존 경로가 외부 URL이면 삭제 호출하지 않음")
    void updateAlbumImage_oldPathExternal_noDelete() {
        // given
        String albumName = "앨범-" + UUID.randomUUID();
        Album album = savedAlbum(albumName, LocalDate.of(2024, 1, 1));

        MockMultipartFile newImage = new MockMultipartFile("albumImage","new.png","image/png","dummy".getBytes());

        // when
        albumAdminService.updateAlbumImage(album.getAlbumId(), newImage);

        // then
        Mockito.verify(fileStorageService, Mockito.never()).deleteFileByPath(Mockito.anyString());
    }

    @Test
    @DisplayName("앨범 비활성화 성공 - 앨범과 소속 곡이 함께 비활성화된다")
    void deleteAlbum_success() {
        // given
        Album album = savedAlbum("앨범-" + UUID.randomUUID(), LocalDate.of(2024, 1, 1));
        savedSong(album, "곡1");
        savedSong(album, "곡2");

        // when
        albumAdminService.deleteAlbum(album.getAlbumId());

        // then - 앨범 상태
        Album deletedAlbum = albumRepository.findById(album.getAlbumId()).orElseThrow();
        assertTrue(deletedAlbum.isDeleted());

        // then - 곡 상태
        List<Song> notDeletedSongs = songRepository.findAllByAlbum_AlbumIdAndIsDeletedFalse(album.getAlbumId());
        assertTrue(notDeletedSongs.isEmpty());

    }

    @Test
    @DisplayName("이미 삭제된 앨범을 비활성화하면 예외 발생")
    void deleteAlbum_alreadyDeleted_throws() {
        // given
        Album album = savedAlbum("앨범-" + UUID.randomUUID(), LocalDate.of(2024, 1, 1));
        album.delete();

        // when
        CustomException exception = assertThrows(CustomException.class, () -> albumAdminService.deleteAlbum(album.getAlbumId()));

        // then
        assertEquals(ErrorCode.ALREADY_IN_REQUESTED_STATE, exception.getErrorCode());
    }

    @Test
    @DisplayName("앨범 활성화 성공 - 앨범과 소속 곡이 함께 복구된다")
    void restoreAlbum_success() {
        // given
        Album album = savedAlbum("앨범-" + UUID.randomUUID(), LocalDate.of(2024, 1, 1));
        Song song1 = savedSong(album, "곡1");
        Song song2 = savedSong(album, "곡2");

        // 먼저 전부 삭제
        album.delete();
        song1.deleteSong();
        song2.deleteSong();

        // when
        albumAdminService.restoreAlbum(album.getAlbumId());

        // then - 앨범 상태
        Album restoredAlbum = albumRepository.findById(album.getAlbumId()).orElseThrow();
        assertFalse(restoredAlbum.isDeleted());

        // then - 곡 상태
        List<Song> deletedSongs = songRepository.findAllByAlbum_AlbumIdAndIsDeletedTrue(album.getAlbumId());
        assertTrue(deletedSongs.isEmpty());
    }

    @Test
    @DisplayName("활성 상태 앨범을 복구하면 예외 발생")
    void restoreAlbum_notDeleted_throws() {
        // given
        Album album = savedAlbum("앨범-" + UUID.randomUUID(), LocalDate.of(2024, 1, 1));

        // when
        CustomException exception = assertThrows(CustomException.class, () -> albumAdminService.restoreAlbum(album.getAlbumId()));

        // then
        assertEquals(ErrorCode.ALREADY_IN_REQUESTED_STATE, exception.getErrorCode());
    }
}