package com.example.ex02.festival.repository;

import com.example.ex02.festival.entity.FestivalEntity;
import com.example.ex02.festival.entity.FestivalTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FestivalTagRepository extends JpaRepository<FestivalTagEntity, Long> {
    List<FestivalTagEntity> findByFestival(FestivalEntity festival);

    // 이번 달에 조회된 모든 축제 번호들에 대해 태그 한 번에 가져오기
    List<FestivalTagEntity> findByFestival_FestivalNoIn(List<Long> festivalNos);
}
