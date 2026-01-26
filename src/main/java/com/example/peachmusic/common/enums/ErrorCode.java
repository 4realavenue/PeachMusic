package com.example.peachmusic.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400
    AUTH_EMAIL_PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "이메일과 비밀번호를 입력해 주세요."),
    AUTH_EMAIL_PASSWORD_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "올바른 이메일 형식이 아닙니다."),
    SEARCH_KEYWORD_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "검색 키워드를 다시 입력해 주세요."),
    PLAYLIST_ADD_SONG_REQUIRED(HttpStatus.BAD_REQUEST, "플레이리스트에 담을 음원을 입력해 주세요."),
    PLAYLIST_REMOVE_SONG_REQUIRED(HttpStatus.BAD_REQUEST, "플레이리스트에서 제거할 음원을 입력해 주세요."),
    AUTH_PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "이메일과 비밀번호를 입력해 주세요."),
    AUTH_INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 올바르지 않습니다."),
    AUTH_NAME_NICKNAME_REQUIRED(HttpStatus.BAD_REQUEST, "이름과 닉네임을 입력해 주세요."),
    ALBUM_UPDATE_NO_CHANGES(HttpStatus.BAD_REQUEST, "수정할 값이 없습니다."),
    ALBUM_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "앨범 이름 입력은 필수입니다."),
    AUTH_EMAIL_CERTIFICATION_FAILED(HttpStatus.BAD_REQUEST, "이메일 인증에 실패 했습니다."),
    AUTH_TOKEN_REQUIRED(HttpStatus.BAD_REQUEST, "토큰에 버전 정보가 없습니다."),
    FILE_REQUIRED(HttpStatus.BAD_REQUEST, "파일은 등록은 필수입니다."),
    IMAGE_TOO_LARGE(HttpStatus.BAD_REQUEST, "이미지 파일 용량이 너무 큽니다."),
    IMAGE_INVALID_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 이미지 파일 형식입니다."),
    AUDIO_TOO_LARGE(HttpStatus.BAD_REQUEST, "음원 파일 용량이 너무 큽니다."),
    AUDIO_INVALID_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 음원 파일 형식입니다."),
    ARTIST_UPDATE_NO_CHANGES(HttpStatus.BAD_REQUEST, "수정할 값이 없습니다."),
    ARTIST_DEBUT_DATE_INVALID(HttpStatus.BAD_REQUEST, "데뷔일은 오늘 이후일 수 없습니다."),

    // 401
    AUTH_CERTIFICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "로그인이 필요한 서비스 입니다."),

    // 403
    AUTH_AUTHORIZATION_REQUIRED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),


    // 404
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저가 존재하지 않습니다."),
    ARTIST_NOT_FOUND(HttpStatus.NOT_FOUND, "아티스트가 존재하지 않습니다."),
    ALBUM_NOT_FOUND(HttpStatus.NOT_FOUND, "앨범이 존재하지 않습니다."),
    SONG_NOT_FOUND(HttpStatus.NOT_FOUND, "음원이 존재하지 않습니다."),
    PLAYLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "플레이리스트가 존재하지 않습니다."),
    PLAYLIST_NOT_FOUND_SONG(HttpStatus.NOT_FOUND, "해당 곡이 플레이리스트에 존재하지 않습니다."),
    ALBUM_DETAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "앨범을 찾을 수 없습니다."),
    ARTIST_DETAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "아티스트를 찾을 수 없습니다."),


    // 409
    USER_EXIST_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일 입니다."),
    USER_EXIST_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임 입니다."),
    PLAYLIST_EXIST_SONG(HttpStatus.CONFLICT, "동일한 곡이 이미 플레이리스트에 있습니다."),
    ALBUM_EXIST_SONG_POSITION(HttpStatus.CONFLICT, "해당 앨범의 수록 번호에 이미 음원이 존재합니다."),
    USER_EXIST_ACTIVATION_USER(HttpStatus.CONFLICT, "이미 활성화 된 유저입니다."),
    USER_EXIST_ROLE(HttpStatus.CONFLICT, "이미 부여된 권한입니다."),
    USER_EXIST_DELETED(HttpStatus.CONFLICT, "이미 비활성화 된 유저입니다."),
    ALBUM_EXIST_NAME_RELEASE_DATE(HttpStatus.CONFLICT, "이미 동일한 앨범이 존재합니다."),
    ALBUM_EXIST_NAME_RELEASE_DATE_DELETED(HttpStatus.CONFLICT, "이미 비활성화 된 앨범입니다."),
    SONG_EXIST_NAME(HttpStatus.CONFLICT, "해당 앨범에 이미 동일한 제목의 음원이 존재합니다."),
    SONG_EXIST_ACTIVATION_SONG(HttpStatus.CONFLICT, "이미 활성화 된 음원입니다."),
    ALBUM_EXIST_IMAGE(HttpStatus.CONFLICT, "이미 동일한 앨범 이미지가 존재합니다."),

    // 500
    SEARCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "일시적인 오류로 검색에 실패했습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    FILE_DELETED_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제에 실패했습니다.")
    ;

    private final HttpStatus status;
    private final String message;
}
