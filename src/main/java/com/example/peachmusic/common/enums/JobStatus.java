package com.example.peachmusic.common.enums;

public enum JobStatus {

    // url만 있는 상태
    NOT_READY,

    // url -> mp3 다운로드 중
    DOWNLOADING,

    // url -> mp3 다운로드 실패
    DOWNLOAD_FAILED,

    // mp3만 있는 상태
    READY,

    // mp3 -> m3u8,ts 형 변환 중
    TRANSCODING,

    // mp3 -> m3u8, ts 형 변환 실패
    TRANSCODE_FAILED,

    // 스트리밍 준비 완료
    SUCCESS

    ;
}
