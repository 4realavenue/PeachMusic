package com.example.peachmusic.domain.openApi.jamendo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JamendoTagDto {
    @JsonProperty("genres")
    private List<String> genres;

    @JsonProperty("instruments")
    private List<String> instruments;

    @JsonProperty("vartags")
    private List<String> moods;
}