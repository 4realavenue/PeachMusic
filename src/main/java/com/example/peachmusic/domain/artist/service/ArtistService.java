package com.example.peachmusic.domain.artist.service;

import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.common.exception.ErrorCode;
import com.example.peachmusic.domain.artist.entity.Artist;
import com.example.peachmusic.domain.artist.model.response.ArtistGetDetailResponseDto;
import com.example.peachmusic.domain.artist.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;

    /**
     * 아티스트 단건 조회 기능
     * @param artistId 조회할 아티스트 ID
     * @return 조회한 아티스트 정보
     */
    @Transactional(readOnly = true)
    public ArtistGetDetailResponseDto getArtistDetail(Long artistId) {

        // 조회 대상 아티스트 조회 (삭제된 아티스트는 조회 불가)
        Artist foundArtist = artistRepository.findByArtistIdAndIsDeletedFalse(artistId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTIST_NOT_FOUND));

        return ArtistGetDetailResponseDto.from(foundArtist);
    }
}
