package com.example.peachmusic.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import org.springframework.data.domain.Slice;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class SliceResponse<T> {

    private final boolean success;
    private final String message;
    private final SliceData<T> data;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;

    private SliceResponse(boolean success, String message, SliceData<T> data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // 커스텀 메시지와 함께
    public static <T> SliceResponse<T> success(String message, Slice<T> slice) {
        return new SliceResponse<>(true, message, new SliceData<>(slice));
    }

    // 실패 응답
    public static <Void> SliceResponse<Void> error(String message) {
        return new SliceResponse<>(false, message, null);
    }

    @Getter
    public static class SliceData<T> {
        private final List<T> content;
        private final boolean hasNext;
        private final int size;
        private final int number;

        public SliceData(Slice<T> slice) {
            this.content = slice.getContent();
            this.hasNext = slice.hasNext();
            this.size = slice.getSize();
            this.number = slice.getNumber();
        }
    }
}