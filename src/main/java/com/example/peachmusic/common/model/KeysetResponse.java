package com.example.peachmusic.common.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class KeysetResponse<T> {

    private final List<T> content;
    private final boolean hasNext;
    private final Cursor cursor;
}
