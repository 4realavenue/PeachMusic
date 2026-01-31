package com.example.peachmusic.common.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Cursor {

    private final Long lastId;
    private final Object lastSortValue;
}