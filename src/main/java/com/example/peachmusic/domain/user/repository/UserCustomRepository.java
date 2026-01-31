package com.example.peachmusic.domain.user.repository;

import com.example.peachmusic.domain.user.dto.response.admin.UserAdminGetResponseDto;
import java.util.List;

public interface UserCustomRepository {

    List<UserAdminGetResponseDto> findUserKeysetPageByWord(String word, int size, Long lastId);
}
