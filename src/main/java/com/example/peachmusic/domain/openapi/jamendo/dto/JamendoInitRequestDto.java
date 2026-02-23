package com.example.peachmusic.domain.openapi.jamendo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class JamendoInitRequestDto {

    @NotNull(message = "startDate는 필수입니다.")
    private LocalDate startDate;

    @NotNull(message = "endDate는 필수입니다.")
    private LocalDate endDate;
}
