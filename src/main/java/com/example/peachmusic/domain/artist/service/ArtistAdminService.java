package com.example.peachmusic.domain.artist.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.exception.ErrorCode;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.model.request.ArtistCreateRequestDto;
import com.example.peachmusic.domain.artist.model.request.ArtistUpdateRequestDto;
import com.example.peachmusic.domain.artist.model.response.ArtistCreateResponseDto;
import com.example.peachmusic.domain.artist.model.response.ArtistSearchResponse;
import com.example.peachmusic.domain.artist.model.response.ArtistUpdateResponseDto;
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
import java.util.Optional;
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

        String artistName = requestDto.getArtistName();

        Optional<Artist> found = artistRepository.findByArtistName(artistName);

        // 동일한 아티스트 이름이 이미 존재하는 경우
        if (found.isPresent()) {
            Artist artist = found.get();

            // 비활성 상태(isDeleted=true)인 경우: 복구 대상이므로 생성 불가
            if (artist.isDeleted()) {
                throw new CustomException(ErrorCode.ARTIST_EXIST_NAME_DELETED);
            }

            // 활성 상태(isDeleted=false)인 경우: 중복 이름으로 생성 불가
            throw new CustomException(ErrorCode.ARTIST_EXIST_NAME);
        }

        // 요청 값으로 아티스트 엔티티 생성 및 저장
        Artist artist = new Artist(artistName);
        Artist savedArtist = artistRepository.save(artist);

        return ArtistCreateResponseDto.from(savedArtist);
    }

    /**
     * 전체 아티스트 조회 기능 (관리자 전용)
     * @param pageable 페이지네이션 및 정렬 정보 (기본 정렬: createdAt DESC)
     * @return 아티스트 목록 페이징 조회 결과
     */
    @Transactional(readOnly = true)
    public Page<ArtistSearchResponse> getArtistList(String word, Pageable pageable) {
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
            throw new CustomException(ErrorCode.ARTIST_EXIST_NAME);
        }

        // 아티스트 이름 변경
        foundArtist.updateArtistName(newName);

        return ArtistUpdateResponseDto.from(foundArtist);
    }

    /**
     * 아티스트 비활성화 기능 (관리자 전용)
     * @param artistId 비활성화할 아티스트 ID
     */
    @Transactional
    public void deleteArtist(Long artistId) {

        // 비활성화 대상 아티스트 조회 (이미 비활성화된 아티스트는 제외)
        Artist foundArtist = artistRepository.findByArtistIdAndIsDeletedFalse(artistId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTIST_NOT_FOUND));

        // 아티스트가 발매한 앨범 비활성화
        List<Album> foundAlbumList = artistAlbumRepository.findAlbumsByArtistIdAndIsDeleted(artistId, false);

        // 앨범 ID 목록 추출
        List<Long> albumIds = foundAlbumList.stream()
                .map(Album::getAlbumId)
                .toList();

        // 앨범에 포함된 음원 비활성화 (앨범들이 없으면 스킵)
        if (!albumIds.isEmpty()) {
            List<Song> foundSongList = songRepository.findAllByAlbum_AlbumIdInAndIsDeletedFalse(albumIds);
            foundSongList.forEach(Song::deleteSong);
        }

        // 앨범 비활성화
        foundAlbumList.forEach(Album::delete);

        // 아티스트 비활성화
        foundArtist.delete();
    }

    /**
     * 아티스트 활성화 기능 (관리자 전용)
     * @param artistId 활성화할 아티스트 ID
     */
    @Transactional
    public void restoreArtist(Long artistId) {

        // 활성화 대상 아티스트 조회 (삭제된 아티스트만 복구 가능)
        Artist foundArtist = artistRepository.findByArtistIdAndIsDeletedTrue(artistId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTIST_NOT_FOUND));

        // 아티스트가 발매한 앨범 활성화
        List<Album> foundAlbumList = artistAlbumRepository.findAlbumsByArtistIdAndIsDeleted(artistId, true);

        // 앨범 ID 목록 추출
        List<Long> albumIds = foundAlbumList.stream()
                .map(Album::getAlbumId)
                .toList();

        // 앨범에 포함된 음원 활성화
        if (!albumIds.isEmpty()) {
            List<Song> foundSongList = songRepository.findAllByAlbum_AlbumIdInAndIsDeletedTrue(albumIds);
            foundSongList.forEach(Song::restoreSong);
        }

        // 앨범 활성화
        foundAlbumList.forEach(Album::restore);

        // 아티스트 활성화
        foundArtist.restore();
    }
}
