package com.example.peachmusic.common.service;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.model.SearchConditionParam;
import java.time.LocalDate;
import static com.example.peachmusic.common.enums.SortType.*;

public abstract class AbstractKeysetService {

    /**
     * 커서가 정렬 기준에 맞게 입력됐는지 검증
     */
    protected void validateCursor(SearchConditionParam condition) {
        SortType sortType = condition.getSortType();
        boolean missingLastLike = sortType == LIKE && condition.getLastId() != null && condition.getLastLike() == null;
        boolean missingLastName = sortType == NAME && condition.getLastId() != null && condition.getLastName() == null;
        if (missingLastLike || missingLastName) {
            throw new CustomException(ErrorCode.MISSING_CURSOR_PARAMETER);
        }
    }

    /**
     * 아티스트 전용 - 커서가 정렬 기준에 맞게 입력됐는지 검증
     * @param sortType 정렬 기준
     * @param lastId 커서 - 마지막 ID
     * @param lastDate 커서 - 마지막 날짜
     */
    protected void validateArtistCursor(SortType sortType, Long lastId, LocalDate lastDate) {
        boolean missingLastDate = sortType == RELEASE_DATE && lastId != null && lastDate == null;
        if (missingLastDate) {
            throw new CustomException(ErrorCode.MISSING_CURSOR_PARAMETER);
        }
    }

    /**
     * 정렬 방향 결정
     * - direction이 있으면 그대로 사용
     * - direction이 null이면 sortType에 맞는 기본값을 반환
     * @param sortType 정렬 기준
     * @param direction 정렬 방향
     * @return 결정된 정렬 방향
     */
    protected SortDirection resolveSortDirection(SortType sortType, SortDirection direction) {
        if (direction != null) {
            return direction;
        }
        return sortType.getDefaultDirection();
    }

}
