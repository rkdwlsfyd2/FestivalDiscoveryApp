package com.example.ex02.festival.repository;

import com.example.ex02.festival.entity.FestivalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FestivalRepository extends JpaRepository<FestivalEntity, Long> {

    @Query("SELECT f FROM FestivalEntity f " +
            "WHERE f.eventStartDate <= :endDate " +
            "AND f.eventEndDate >= :startDate " +
            "AND f.isActive = 'Y' " +
            "AND (:region IS NULL OR :region = '' OR f.addr LIKE %:region%)")
    List<FestivalEntity> findFestivalsForMonth(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("region") String region
    );
}
