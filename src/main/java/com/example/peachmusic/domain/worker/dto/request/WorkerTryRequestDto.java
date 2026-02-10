package com.example.peachmusic.domain.worker.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class WorkerTryRequestDto {

    private List<Long> songIdList;

    public WorkerTryRequestDto(List<Long> songIdList) {
        this.songIdList = songIdList;
    }
}
