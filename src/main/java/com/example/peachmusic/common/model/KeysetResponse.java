package com.example.peachmusic.common.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.function.Function;

@Getter
@RequiredArgsConstructor
public class KeysetResponse<T> {

    private final List<T> content;
    private final boolean hasNext;
    private final NextCursor nextCursor;

    public static <T> KeysetResponse<T> of(List<T> content, int size, Function<T, NextCursor> cursorExtractor) {

        boolean hasNext = content.size() > size; // 다음 페이지 존재 여부
        NextCursor nextCursor = null;

        if (hasNext) {
            content.remove(size); // 다음 페이지 삭제

            T last = content.get(content.size() - 1);
            nextCursor = cursorExtractor.apply(last);
        }

        return new KeysetResponse<>(content, hasNext, nextCursor);
    }
}
