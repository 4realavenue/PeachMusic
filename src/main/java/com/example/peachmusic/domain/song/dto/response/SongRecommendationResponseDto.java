package com.example.peachmusic.domain.song.dto.response;

import com.example.peachmusic.domain.artistsong.entity.ArtistSong;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SongRecommendationResponseDto {

    private final Long songId;
    private final String songName;

    private final Long artistId;
    private final String artistName;

    private final Long albumId;
    private final String albumName;
    private final String albumImage;

    private final Long likeCount;

    public static SongRecommendationResponseDto from(ArtistSong artistSong) {
        return new SongRecommendationResponseDto(
                artistSong.getSong().getSongId(),
                artistSong.getSong().getName(),
                artistSong.getArtist().getArtistId(),
                artistSong.getArtist().getArtistName(),
                artistSong.getSong().getAlbum().getAlbumId(),
                artistSong.getSong().getAlbum().getAlbumName(),
                artistSong.getSong().getAlbum().getAlbumImage(),
                artistSong.getSong().getLikeCount()
        );
    }
}
