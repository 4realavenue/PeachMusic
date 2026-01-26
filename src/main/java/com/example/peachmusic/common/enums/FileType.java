package com.example.peachmusic.common.enums;

public enum FileType {
    ALBUM_IMAGE("albumImages"),
    ARTIST_PROFILE("profileImages"),
    AUDIO("audios"),
    PLAYLIST_IMAGE("playlistImages");

    private final String folder;

    FileType(String folder) {
        this.folder = folder;
    }

    public String folder() {
        return folder;
    }
}
