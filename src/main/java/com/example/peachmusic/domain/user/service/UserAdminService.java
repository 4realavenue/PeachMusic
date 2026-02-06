package com.example.peachmusic.domain.user.service;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.enums.UserRole;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.model.Cursor;
import com.example.peachmusic.common.model.KeysetResponse;
import com.example.peachmusic.common.service.AbstractKeysetService;
import com.example.peachmusic.domain.user.dto.response.admin.UserAdminGetResponseDto;
import com.example.peachmusic.domain.user.entity.User;
import com.example.peachmusic.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static com.example.peachmusic.common.constants.SearchViewSize.DETAIL_SIZE;

@Service
@RequiredArgsConstructor
public class UserAdminService extends AbstractKeysetService {

    private final UserRepository userRepository;

    /**
     *  (관리자) 유저 목록 조회
     */
    @Transactional(readOnly = true)
    public KeysetResponse<UserAdminGetResponseDto> getUserList(String word, Long lastId) {
        final int size = DETAIL_SIZE;
        List<UserAdminGetResponseDto> content = userRepository.findUserKeysetPageByWord(word, size, lastId);

        return toKeysetResponse(content, size, last -> new Cursor(last.getUserId(), null));
    }

    /**
     *  (관리자) 유저 비활성화
     */
    @Transactional
    public void deleteUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.isDeleted()) {
            throw new CustomException(ErrorCode.USER_EXIST_DELETED);
        }
        user.delete();
    }

    /**
     *  (관리자) 유저 활성화
     */
    @Transactional
    public void restorationUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!user.isDeleted()) {
            throw new CustomException(ErrorCode.USER_EXIST_ACTIVATION_USER);
        }
        user.restore();
    }

    /**
     *  (관리자) 유저 권한부여
     */
    @Transactional
    public void role(Long userId, UserRole newRole) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (user.getRole() == newRole) {
            throw new CustomException(ErrorCode.USER_EXIST_ROLE);
        }
        user.setRole(newRole);
    }
}