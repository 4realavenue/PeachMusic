package com.example.peachmusic.domain.song.service;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.model.AuthUser;
import com.example.peachmusic.domain.album.entity.Album;
import com.example.peachmusic.domain.album.repository.AlbumRepository;
import com.example.peachmusic.domain.song.dto.response.SongGetDetailResponseDto;
import com.example.peachmusic.domain.song.dto.response.SongSearchResponseDto;
import com.example.peachmusic.domain.song.entity.Song;
import com.example.peachmusic.domain.song.repository.SongRepository;
import com.example.peachmusic.domain.songGenre.entity.SongGenre;
import com.example.peachmusic.domain.songGenre.repository.SongGenreRepository;
import com.example.peachmusic.domain.songLike.repository.SongLikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static com.example.peachmusic.common.enums.UserRole.USER;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;
    private final SongGenreRepository songGenreRepository;
    private final AlbumRepository albumRepository;
    private final SongLikeRepository songLikeRepository;

    /**
     * 음원 단건 조회
     */
    @Transactional(readOnly = true)
    public SongGetDetailResponseDto getSong(Long songId, AuthUser authUser) {

        Song findSong = songRepository.findBySongIdAndIsDeletedFalse(songId)
                .orElseThrow(() -> new CustomException(ErrorCode.SONG_NOT_FOUND));

        boolean liked = false;

        if (authUser != null) { // 로그인이 된 경우
            log.info("authUserId {}", authUser.getUserId());

            if (songLikeRepository.existsSongLikeByUserAndSong(authUser.getUser(), findSong)) {
                liked = true;
            }
        }

        Long findAlbumId = songRepository.findSongs_AlbumIdBySongId(findSong);

        Album findAlbum = albumRepository.findByAlbumIdAndIsDeletedFalse(findAlbumId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALBUM_NOT_FOUND));

        List<SongGenre> findSongGenreList = songGenreRepository.findAllBySong(findSong);

        List<String> genreNameList = findSongGenreList.stream()
                .map(songGenre -> songGenre.getGenre().getGenreName())
                .toList();

        return SongGetDetailResponseDto.from(findSong, genreNameList, findAlbum, liked);

    }

    /**
     * 음원 검색 - 자세히 보기
     *
     * @param word     검색어
     * @param pageable 페이징 정보 - 인기순 정렬
     * @return 페이징된 음원 검색 응답 DTO
     */
    @Transactional(readOnly = true)
    public Page<SongSearchResponseDto> searchSongPage(String word, Pageable pageable) {
        return songRepository.findSongPageByWord(word, pageable, USER);
    }

    /**
     * 음원 검색 - 미리보기
     *
     * @param word 검색어
     * @return 음원 검색 응답 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<SongSearchResponseDto> searchSongList(String word) {
        final int limit = 5;
        return songRepository.findSongListByWord(word, limit);
    }
}
