package com.example.ex02.festival.repository;

import com.example.ex02.festival.entity.FestivalEntity;
import com.example.ex02.festival.entity.FestivalImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FestivalImageRepository extends JpaRepository<FestivalImageEntity, Long> {
    List<FestivalImageEntity> findByFestival(FestivalEntity festival);
}
