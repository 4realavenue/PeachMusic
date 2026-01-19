package com.example.peachmusic.domain.user.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL) // null을 받으면 업데이트 안함.
public class UserUpdateRequestDto {

    private String name;

    private String nickname;



}
