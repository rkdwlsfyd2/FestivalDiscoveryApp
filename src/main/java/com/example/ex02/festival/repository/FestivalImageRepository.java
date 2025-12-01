package com.example.ex02.festival.repository;

import com.example.ex02.festival.entity.FestivalImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FestivalImageRepository extends JpaRepository<FestivalImageEntity, Long> {
    Optional<FestivalImageEntity> findFirstByFestival_FestivalNo(Long festivalNo);
}
