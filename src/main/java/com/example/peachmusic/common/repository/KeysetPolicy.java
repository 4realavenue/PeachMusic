package com.example.peachmusic.common.repository;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.model.CursorParam;
import org.springframework.stereotype.Component;

@Component
public class KeysetPolicy {

    /**
     * 커서가 정렬 기준에 맞게 입력됐는지 검증
     */
    public void validateCursor(SortType sortType, CursorParam cursor) {
        if (sortType.isCursorInvalid(cursor)) {
            throw new CustomException(ErrorCode.MISSING_CURSOR_PARAMETER);
        }
    }

    /**
     * 정렬 방향 결정하여 ASC인지 판단
     * - direction이 있으면 그대로 사용
     * - direction이 null이면 sortType에 맞는 기본값을 반환
     * @param sortType 정렬 기준
     * @param direction 정렬 방향
     * @return 결정된 정렬 방향
     */
    public boolean isAscending(SortType sortType, SortDirection direction) {
        if (sortType == null) {
            return true; // sortType 없으면 ASC
        }

        direction = direction != null ? direction : sortType.getDefaultDirection();
        return direction == SortDirection.ASC;
    }

}
