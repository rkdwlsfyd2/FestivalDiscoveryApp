package com.example.ex02.festival.repository;

import com.example.ex02.festival.entity.FestivalEntity;
import com.example.ex02.festival.entity.FestivalTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FestivalTagRepository extends JpaRepository<FestivalTagEntity, Long> {
    List<FestivalTagEntity> findByFestival(FestivalEntity festival);
}
