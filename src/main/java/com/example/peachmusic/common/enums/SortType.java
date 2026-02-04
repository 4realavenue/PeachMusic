package com.example.peachmusic.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import static com.example.peachmusic.common.enums.SortDirection.ASC;
import static com.example.peachmusic.common.enums.SortDirection.DESC;

@Getter
@RequiredArgsConstructor
public enum SortType {
    LIKE(DESC), // 좋아요는 내림차순 (많은 순)
    NAME(ASC), // 이름은 오름차순
    RELEASE_DATE(DESC) // 발매일은 내림차순(최신 순)
    ;

    private final SortDirection defaultDirection; // 기본 정렬 방향
}
