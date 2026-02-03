package com.example.peachmusic.domain.worker.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class WorkerRetryRequestDto {

    private List<Long> songIdList;

}
