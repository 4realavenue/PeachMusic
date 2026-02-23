package com.example.peachmusic.domain.artist.service;

import com.example.peachmusic.common.enums.ArtistType;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.enums.FileType;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.storage.FileStorageService;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.artist.dto.request.ArtistCreateRequestDto;
import com.example.peachmusic.domain.artist.dto.request.ArtistUpdateRequestDto;
import com.example.peachmusic.domain.artist.dto.response.ArtistCreateResponseDto;
import com.example.peachmusic.domain.artist.dto.response.ArtistImageUpdateResponseDto;
import com.example.peachmusic.domain.artist.dto.response.ArtistUpdateResponseDto;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import com.example.peachmusic.domain.artistalbum.entity.ArtistAlbum;
import com.example.peachmusic.domain.artistalbum.repository.ArtistAlbumRepository;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.repository.SongRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@ActiveProfiles("test")
@Transactional
class ArtistAdminServiceTest {

    @Autowired
    private ArtistAdminService artistAdminService;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private ArtistAlbumRepository artistAlbumRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FileStorageService fileStorageService;

    @PersistenceContext
    EntityManager em;

    private Artist savedArtist() {
        return artistRepository.save(new Artist(
                "아이유",
                        "artists/old/profile.png",
                        "대한민국",
                        ArtistType.SOLO,
                        LocalDate.of(2024, 1, 1),
                        "안녕하세요."
                )
        );
    }

    private Artist savedArtist(String profileImage) {
        return artistRepository.save(new Artist(
                "아이유",
                profileImage,
                "대한민국",
                ArtistType.SOLO,
                LocalDate.of(2024, 1, 1),
                "안녕하세요."
        ));
    }

    private Album savedAlbum(LocalDate date) {
        return albumRepository.save(new Album("앨범1", date));
    }

    private Song savedSong(Album album) {
        return songRepository.save(new Song(
                album,
                "수록곡1",
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

    private ArtistCreateRequestDto requestDto(LocalDate debutDate) throws Exception {
        String json = """
                {
                    "artistName": "%s",
                    "country": "%s",
                    "artistType": "%s",
                    "debutDate": "%s",
                    "bio": "%s"
                }
                """.formatted("  아이유  ", "대한민국", ArtistType.SOLO.name(), debutDate, "안녕하세요.");
        return objectMapper.readValue(json, ArtistCreateRequestDto.class);
    }

    private ArtistUpdateRequestDto updateRequestDto(String artistName, String country, ArtistType artistType, LocalDate debutDate, String bio) throws Exception {

        ObjectNode node = objectMapper.createObjectNode();

        if (artistName != null) node.put("artistName", artistName);
        if (country != null) node.put("country", country);
        if (artistType != null) node.put("artistType", artistType.name());
        if (debutDate != null) node.put("debutDate", debutDate.toString());
        if (bio != null) node.put("bio", bio);

        return objectMapper.treeToValue(node, ArtistUpdateRequestDto.class);
    }

    @BeforeEach
    void setupStorageMock() {
        Mockito.when(fileStorageService.storeFile(
                Mockito.any(MultipartFile.class),
                Mockito.any(FileType.class),
                Mockito.anyString()
        )).thenReturn("artists/test/profile.png");
    }

    @Test
    @DisplayName("아티스트 생성 성공 - 프로필 이미지 없이 생성하면 디폴트 이미지 경로로 저장된다")
    void createArtist_withoutProfileImage_success() throws Exception {
        // given
        ArtistCreateRequestDto dto = requestDto(LocalDate.of(2024, 1, 1));

        // when
        ArtistCreateResponseDto responseDto = artistAdminService.createArtist(dto, null);

        // then
        Artist saved = artistRepository.findById(responseDto.getArtistId()).orElseThrow();
        assertThat(saved.getArtistName()).isEqualTo("아이유");
        assertThat(saved.getProfileImage()).isEqualTo("https://img.peachmusics.com/storage/image/default-image.jpg");
    }

    @Test
    @DisplayName("아티스트 생성 성공 - 프로필 이미지가 있으면 저장된 경로가 이미지 경로로 저장된다")
    void createArtist_withProfileImage_success() throws Exception {
        // given
        ArtistCreateRequestDto request = requestDto(LocalDate.of(2024, 1, 1));

        MockMultipartFile profileImage = new MockMultipartFile("profileImage", "profile.png", "image/png", "dummy".getBytes());

        // when
        ArtistCreateResponseDto responseDto = artistAdminService.createArtist(request, profileImage);

        // then
        Artist saved = artistRepository.findById(responseDto.getArtistId()).orElseThrow();
        assertThat(saved.getArtistName()).isEqualTo("아이유");
        assertThat(saved.getProfileImage()).isEqualTo("artists/test/profile.png");
    }

    @Test
    @DisplayName("아티스트 기본 정보 수정 성공")
    void updateArtist_basicInfo_success() throws Exception {
        // given
        Artist artist = savedArtist();

        ArtistUpdateRequestDto requestDto =
                updateRequestDto(
                        "아이유 수정",
                        "미국",
                        ArtistType.GROUP,
                        LocalDate.of(2024, 1, 1),
                        "수정된 소개"
                );

        // when
        ArtistUpdateResponseDto responseDto = artistAdminService.updateArtist(artist.getArtistId(), requestDto);

        // then
        Artist updated = artistRepository.findById(responseDto.getArtistId()).orElseThrow();

        assertThat(updated.getArtistName()).isEqualTo("아이유 수정");
        assertThat(updated.getCountry()).isEqualTo("미국");
        assertThat(updated.getArtistType()).isEqualTo(ArtistType.GROUP);
        assertThat(updated.getDebutDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(updated.getBio()).isEqualTo("수정된 소개");

        // 기본 정보 수정이므로 프로필 이미지는 변경되지 않는다
        assertThat(updated.getProfileImage()).isEqualTo("artists/old/profile.png");
    }

    @Test
    @DisplayName("존재하지 않는 artistId면 예외 발생")
    void updateArtist_artistNotFound_throws() throws Exception {
        // given
        ArtistUpdateRequestDto requestDto = updateRequestDto("아무거나", null, null, null, null);

        // when
        CustomException exception = assertThrows(CustomException.class, () -> artistAdminService.updateArtist(999999L, requestDto));

        // then
        assertEquals(ErrorCode.ARTIST_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("아티스트 프로필 이미지 수정 성공 - 기존 이미지가 있으면 삭제 후 새 경로로 업데이트된다")
    void updateProfileImage_withOldImage_success() {
        // given
        String oldPath = "artists/old/profile.png";
        Artist artist = savedArtist(oldPath);

        MockMultipartFile newProfileImage = new MockMultipartFile("profileImage", "profile.png", "image/png", "dummy".getBytes());

        // when
        ArtistImageUpdateResponseDto responseDto = artistAdminService.updateProfileImage(artist.getArtistId(), newProfileImage);

        // then
        Artist updated = artistRepository.findById(responseDto.getArtistId()).orElseThrow();

        // storeFile 스텁이 반환한 값 그대로 들어갔는지만 확인
        assertThat(updated.getProfileImage()).isEqualTo("artists/test/profile.png");

        // oldPath 삭제 호출 확인
        Mockito.verify(fileStorageService, Mockito.times(1)).deleteFileByPath(oldPath);
    }

    @Test
    @DisplayName("아티스트 프로필 이미지 수정 성공 - 기존 이미지가 없으면 삭제를 호출하지 않는다")
    void updateProfileImage_withoutOldImage_success() {
        // given
        Artist artist = savedArtist(null);

        MockMultipartFile newProfileImage = new MockMultipartFile("profileImage", "profile.png", "image/png", "dummy".getBytes());

        // when
        ArtistImageUpdateResponseDto responseDto = artistAdminService.updateProfileImage(artist.getArtistId(), newProfileImage);

        // then
        Artist updated = artistRepository.findById(responseDto.getArtistId()).orElseThrow();

        assertThat(updated.getProfileImage()).isEqualTo("artists/test/profile.png");

        // oldPath가 null이면 삭제 호출 없어야 함
        Mockito.verify(fileStorageService, Mockito.never()).deleteFileByPath(Mockito.anyString());
    }

    @Test
    @DisplayName("존재하지 않는 artistId면 예외 발생")
    void updateProfileImage_artistNotFound_throws() {
        // given
        MockMultipartFile newProfileImage = new MockMultipartFile("profileImage", "profile.png", "image/png", "dummy".getBytes());

        // when
        CustomException exception = assertThrows(CustomException.class, () -> artistAdminService.updateProfileImage(999999L, newProfileImage));

        // then
        assertEquals(ErrorCode.ARTIST_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("아티스트 비활성화 성공 - orphan 앨범이면 연결된 앨범/음원도 함께 soft-delete 된다")
    void deleteArtist_withAlbums_success() {
        // given
        Artist artist = savedArtist(null);

        Album album = savedAlbum(LocalDate.of(2024, 1, 1));
        mapArtistToAlbum(artist, album);

        Song song = savedSong(album);

        // when
        artistAdminService.deleteArtist(artist.getArtistId());

        // then
        em.flush();
        em.clear();

        assertThat(artistRepository.findById(artist.getArtistId()).orElseThrow().isDeleted()).isTrue();
        assertThat(albumRepository.findById(album.getAlbumId()).orElseThrow().isDeleted()).isTrue();
        assertThat(songRepository.findById(song.getSongId()).orElseThrow().isDeleted()).isTrue();
    }

    @Test
    @DisplayName("아티스트 비활성화 - 앨범에 다른 활성 아티스트가 있으면 앨범/음원은 삭제되지 않는다")
    void deleteArtist_notOrphanAlbum_albumNotDeleted() {
        // given
        Artist artistA = savedArtist(null);
        Artist artistB = savedArtist(null);

        Album album = savedAlbum(LocalDate.of(2024, 1, 1));
        mapArtistToAlbum(artistA, album);
        mapArtistToAlbum(artistB, album);

        Song song = savedSong(album);

        // when
        artistAdminService.deleteArtist(artistA.getArtistId());

        // then
        em.flush();
        em.clear();

        assertThat(artistRepository.findById(artistA.getArtistId()).orElseThrow().isDeleted()).isTrue();
        assertThat(albumRepository.findById(album.getAlbumId()).orElseThrow().isDeleted()).isFalse();
        assertThat(songRepository.findById(song.getSongId()).orElseThrow().isDeleted()).isFalse();
    }

    @Test
    @DisplayName("아티스트 비활성화 성공 - 연결된 앨범이 없어도 아티스트만 soft-delete 된다")
    void deleteArtist_withoutAlbums_success() {
        // given
        Artist artist = savedArtist(null);

        // when
        artistAdminService.deleteArtist(artist.getArtistId());

        // then
        em.flush();
        em.clear();

        Artist deletedArtist = artistRepository.findById(artist.getArtistId()).orElseThrow();
        assertThat(deletedArtist.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 artistId면 예외 발생 - 비활성화")
    void deleteArtist_artistNotFound_throws() {
        // given
        Long invalidArtistId = 999999L;

        // when
        CustomException exception = assertThrows(CustomException.class, () -> artistAdminService.deleteArtist(invalidArtistId));

        // then
        assertEquals(ErrorCode.ARTIST_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("아티스트 활성화 성공 - 삭제된 앨범이 활성 아티스트를 다시 가지면 앨범/음원도 restore 된다")
    void restoreArtist_restoreDeletedAlbum_success() {
        Artist artistA = savedArtist(null);
        Artist artistB = savedArtist(null);

        Album album = savedAlbum(LocalDate.of(2024, 1, 1));
        mapArtistToAlbum(artistA, album);
        mapArtistToAlbum(artistB, album);

        Song song = savedSong(album);

        // 둘 다 삭제 -> orphan -> 앨범/음원 삭제됨
        artistAdminService.deleteArtist(artistA.getArtistId());
        artistAdminService.deleteArtist(artistB.getArtistId());

        em.flush();
        em.clear();

        // A만 복구 -> 앨범에 활성 아티스트 생김 -> 앨범/음원 복구되어야 함
        artistAdminService.restoreArtist(artistA.getArtistId());

        em.flush();
        em.clear();

        assertThat(artistRepository.findById(artistA.getArtistId()).orElseThrow().isDeleted()).isFalse();
        assertThat(albumRepository.findById(album.getAlbumId()).orElseThrow().isDeleted()).isFalse();
        assertThat(songRepository.findById(song.getSongId()).orElseThrow().isDeleted()).isFalse();
    }

    @Test
    @DisplayName("아티스트 활성화 성공 - 연결된 앨범이 없어도 아티스트만 restore 된다")
    void restoreArtist_withoutAlbums_success() {
        // given
        Artist artist = savedArtist(null);

        artistAdminService.deleteArtist(artist.getArtistId());
        em.flush();
        em.clear();

        // when
        artistAdminService.restoreArtist(artist.getArtistId());

        // then
        em.flush();
        em.clear();

        Artist restoredArtist = artistRepository.findById(artist.getArtistId()).orElseThrow();
        assertThat(restoredArtist.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 artistId면 예외 발생 - 활성화")
    void restoreArtist_artistNotFound_throws() {
        // given
        Long invalidArtistId = 999999L;

        // when
        CustomException exception = assertThrows(CustomException.class, () -> artistAdminService.restoreArtist(invalidArtistId));

        // then
        assertEquals(ErrorCode.ARTIST_NOT_FOUND, exception.getErrorCode());
    }
}
