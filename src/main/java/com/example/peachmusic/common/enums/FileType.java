package com.example.peachmusic.common.enums;

public enum FileType {
    ALBUM_IMAGE("image/album"),
    ARTIST_PROFILE("image/artist"),
    AUDIO("audio"),
    PLAYLIST_IMAGE("image/playlist");

    private final String folder;

    FileType(String folder) {
        this.folder = folder;
    }

    public String folder() {
        return folder;
    }
}
