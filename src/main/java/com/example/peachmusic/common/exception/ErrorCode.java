package com.example.peachmusic.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400
    AUTH_EMAIL_PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "이메일과 비밀번호를 입력해 주세요."),
    AUTH_EMAIL_PASSWORD_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "올바른 이메일 형식이 아닙니다."),
    SEARCH_KEYWORD_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "검색 키워드를 다시 입력해주세요"),
    AUTH_PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "이메일과 비밀번호를 입력해 주세요."),
    AUTH_INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 올바르지 않습니다."),
    AUTH_NAME_NICKNAME_REQUIRED(HttpStatus.BAD_REQUEST, "이름과 닉네임을 입력해주세요."),



    // 401
    AUTH_EMAIL_CERTIFICATION_FAILED(HttpStatus.BAD_REQUEST, "이메일 인증에 실패 했습니다."),
    AUTH_CERTIFICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "로그인이 필요한 서비스 입니다."),


    // 403
    AUTH_AUTHORIZATION_REQUIRED(HttpStatus.FORBIDDEN, "해당 리소스에 대한 권한이 없습니다."),


    // 404
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저가 존재하지 않습니다."),
    ARTIST_NOT_FOUND(HttpStatus.NOT_FOUND, "아티스트가 존재하지 않습니다."),
    ALBUM_NOT_FOUND_ARTIST(HttpStatus.NOT_FOUND, "참여한 아티스트가 존재하지 않습니다."),
    ALBUM_NOT_FOUND(HttpStatus.NOT_FOUND, "앨범이 존재하지 않습니다."),
    SONG_NOT_FOUND(HttpStatus.NOT_FOUND, "음원이 존재하지 않습니다."),
    PLAYLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "플레이리스트가 존재하지 않습니다."),
    PLAYLIST_NOT_FOUND_SONG(HttpStatus.NOT_FOUND, "해당 곡이 플레이리스트에 존재하지 않습니다."),


    // 409
    USER_EXIST_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일 입니다."),
    USER_EXIST_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임 입니다."),
    PLAYLIST_EXIST_SONG(HttpStatus.CONFLICT, "동일한 곡이 플레이리스트에 있습니다."),
    ARTIST_EXIST_NAME(HttpStatus.CONFLICT, "이미 존재하는 아티스트명입니다."),
    ARTIST_EXIST_NAME_DELETED(HttpStatus.CONFLICT, "비활성화된 동일 이름 아티스트가 존재합니다. 복구 기능을 사용해주세요."),
    USER_EXIST_ACTIVATIONUSER(HttpStatus.CONFLICT, "이미 존재하는 유저 입니다."),
    USER_EEXIST_ROLE(HttpStatus.CONFLICT, "이미 부여된 권한입니다."),
    USER_EXIST_DELETED(HttpStatus.CONFLICT, "이미 삭제된 유저 입니다."),


    // 500
    SEARCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "일시적인 오류로 검색에 실패 했습니다."),

    ;

    private final HttpStatus status;
    private final String message;
}
