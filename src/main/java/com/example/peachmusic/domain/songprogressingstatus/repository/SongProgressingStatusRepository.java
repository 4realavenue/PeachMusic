package com.example.peachmusic.domain.songprogressingstatus.repository;

import com.example.peachmusic.domain.songprogressingstatus.entity.SongProgressingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongProgressingStatusRepository extends JpaRepository<SongProgressingStatus, Long> {
}
