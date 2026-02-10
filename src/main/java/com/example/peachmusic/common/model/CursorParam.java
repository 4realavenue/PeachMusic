package com.example.peachmusic.common.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CursorParam {

    private Long lastId;
    private Long lastLike;
    private String lastName;
    private LocalDate lastDate;
    private Long lastPlay;
}
