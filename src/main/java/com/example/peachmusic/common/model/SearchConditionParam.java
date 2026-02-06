package com.example.peachmusic.common.model;

import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class SearchConditionParam {

    @NotBlank(message = "검색어를 입력해주세요.")
    private String word;

    private SortType sortType = SortType.LIKE; // 정렬 기준: 기본값은 좋아요순
    private SortDirection direction; // 정렬 방향

    // keyset cursor
    private Long lastId;
    private Long lastLike;
    private String lastName;
    private LocalDate lastDate;

}
