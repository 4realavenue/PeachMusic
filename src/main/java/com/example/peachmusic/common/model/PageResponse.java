package com.example.peachmusic.common.model;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PageResponse<T> {

    private final boolean success;
    private final String message;
    private final PageData<T> data;
    private final LocalDateTime timestamp;

    public PageResponse(boolean success, String message, PageData<T> data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // 페이지 응답
    public static <T> PageResponse<T> success(String message, Page<T> page) {
        return new PageResponse<>(true, message, new PageData<>(page));
    }



    /**
     * 내부 클래스
     * @param <T>
     */
    @Getter
    public static class PageData<T> {

        private final List<T> content;
        private final long totalElements;
        private final int totalPages;
        private final int size;
        private final int number;

        public PageData(Page<T> page) {
            this.content = page.getContent();
            this.totalElements = page.getTotalElements();
            this.totalPages = page.getTotalPages();
            this.size = page.getSize();
            this.number = page.getNumber();
        }

    }
}
