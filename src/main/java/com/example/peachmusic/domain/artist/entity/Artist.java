package com.example.peachmusic.domain.artist.entity;

import com.example.peachmusic.common.model.BaseEntity;
import com.example.peachmusic.common.enums.ArtistType;
import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.exception.CustomException;
import com.example.peachmusic.domain.artist.dto.request.ArtistUpdateRequestDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table (name = "artists")
public class Artist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "artist_id")
    private Long artistId;

    @Column(name = "artist_name", nullable = false)
    private String artistName;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "country")
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "artist_type")
    private ArtistType artistType;

    @Column(name = "debut_date")
    private LocalDate debutDate;

    @Column(name = "bio")
    private String bio;

    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "jamendo_artist_id", unique = true)
    private Long jamendoArtistId;

    public Artist(String artistName, String profileImage, String country, ArtistType artistType, LocalDate debutDate, String bio) {
        this.artistName = artistName;
        this.profileImage = profileImage;
        this.country = country;
        this.artistType = artistType;
        this.debutDate = debutDate;
        this.bio = bio;
    }

    public Artist(String artistName, Long jamendoArtistId) {
        this.artistName = artistName;
        this.jamendoArtistId = jamendoArtistId;
    }

    public void updateArtistInfo(ArtistUpdateRequestDto requestDto) {
        this.artistName = (requestDto.getArtistName() == null || requestDto.getArtistName().isBlank()) ? this.artistName : requestDto.getArtistName().trim();
        this.country = (requestDto.getCountry() == null || requestDto.getCountry().isBlank()) ? this.country : requestDto.getCountry().trim();
        this.bio = (requestDto.getBio() == null || requestDto.getBio().isBlank()) ? this.bio : requestDto.getBio().trim();

        if (requestDto.getArtistType() != null) {
            this.artistType = requestDto.getArtistType();
        }

        if (requestDto.getDebutDate() != null && requestDto.getDebutDate().isAfter(LocalDate.now())) {
            throw new CustomException(ErrorCode.ARTIST_DEBUT_DATE_INVALID);
        }
        this.debutDate = requestDto.getDebutDate();
    }

    public void delete() {
        this.isDeleted = true;
    }

    public void restore() {
        this.isDeleted = false;
    }

    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
