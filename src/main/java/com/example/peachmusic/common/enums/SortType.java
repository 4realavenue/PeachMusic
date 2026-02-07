package com.example.peachmusic.common.enums;

import com.example.peachmusic.common.model.CursorParam;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.function.Function;
import static com.example.peachmusic.common.enums.SortDirection.*;

@Getter
@RequiredArgsConstructor
public enum SortType {
    LIKE(DESC, CursorParam::getLastLike), // 좋아요순
    NAME(ASC, CursorParam::getLastName), // 이름순
    RELEASE_DATE(DESC, CursorParam::getLastDate), // 발매일순
    PLAY(DESC, CursorParam::getLastPlay), // 재생순
    ;

    private final SortDirection defaultDirection; // 기본 정렬 방향
    private final Function<CursorParam, Object> requiredCursor; // 요구하는 커서

    public boolean isCursorInvalid(CursorParam cursor) {

        if (cursor == null) {
            return false; // 커서 자체가 없으면 첫 페이지로 간주
        }

        boolean hasLastId = cursor.getLastId() != null;
        boolean hasRequiredCursor = requiredCursor.apply(cursor) != null;

        return hasLastId ^ hasRequiredCursor; // 둘중에 하나가 없는 경우
    }
}
