package com.example.peachmusic.domain.openApi.jamendo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JamendoSongResponseDto {
    @JsonProperty("results")
    private List<JamendoSongDto> results;
}