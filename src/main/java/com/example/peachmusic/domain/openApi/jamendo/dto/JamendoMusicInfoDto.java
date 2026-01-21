package com.example.peachmusic.domain.openApi.jamendo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JamendoMusicInfoDto {
    @JsonProperty("vocalinstrumental")
    private String vocalInstrumental;

    @JsonProperty("lang")
    private String lang;

    @JsonProperty("speed")
    private String speed;

    @JsonProperty("tags")
    private JamendoTagDto tags;
}
