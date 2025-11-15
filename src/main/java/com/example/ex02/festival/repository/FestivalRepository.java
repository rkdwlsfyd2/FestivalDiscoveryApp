package com.example.ex02.festival.repository;

import com.example.ex02.festival.entity.FestivalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FestivalRepository extends JpaRepository<FestivalEntity, Long> {

    List<FestivalEntity> findByTitleContaining(String keyword);
}
