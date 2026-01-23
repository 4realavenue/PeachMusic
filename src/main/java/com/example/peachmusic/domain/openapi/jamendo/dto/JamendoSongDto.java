package com.example.peachmusic.domain.openapi.jamendo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JamendoSongDto {
    @JsonProperty("id")
    private Long jamendoSongId;

    @JsonProperty("name")
    private String jamendoSongName;

    @JsonProperty("duration")
    private Integer jamendoDuration;

    @JsonProperty("license_ccurl")
    private String jamendoLicenseCcurl;

    @JsonProperty("position")
    private Integer jamendoPosition;

    @JsonProperty("releasedate")
    private LocalDate jamendoAlbumReleaseDate;

    @JsonProperty("audio")
    private String jamendoAudioUrl;

    @JsonProperty("artist_id")
    private Long jamendoArtistId;

    @JsonProperty("artist_name")
    private String jamendoArtistName;

    @JsonProperty("album_id")
    private Long jamendoAlbumId;

    @JsonProperty("album_name")
    private String jamendoAlbumName;

    @JsonProperty("album_image")
    private String jamendoAlbumImage;

    @JsonProperty("musicinfo")
    private JamendoMusicInfoDto jamendoMusicInfo;
}