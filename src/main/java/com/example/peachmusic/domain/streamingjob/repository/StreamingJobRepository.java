package com.example.peachmusic.domain.streamingjob.repository;

import com.example.peachmusic.domain.streamingjob.entity.StreamingJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StreamingJobRepository extends JpaRepository<StreamingJob, Long> {
}
