package com.example.peachmusic.domain.openapi.jamendo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JamendoSongDto {
    @JsonProperty("id")
    private String jamendoSongId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("duration")
    private Integer duration;

    @JsonProperty("license_ccurl")
    private String licenseCcurl;

    @JsonProperty("position")
    private Integer position;

    @JsonProperty("releasedate")
    private LocalDate albumReleaseDate;

    @JsonProperty("audio")
    private String audioUrl;

    @JsonProperty("artist_id")
    private String artistId;

    @JsonProperty("artist_name")
    private String artistName;

    @JsonProperty("album_id")
    private String albumId;

    @JsonProperty("album_name")
    private String albumName;

    @JsonProperty("album_image")
    private String albumImage;

    @JsonProperty("musicinfo")
    private JamendoMusicInfoDto musicInfo;
}