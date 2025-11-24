package com.example.ex02.festival.repository;

import com.example.ex02.festival.entity.FestivalDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FestivalDetailRepository extends JpaRepository<FestivalDetailEntity, Long> {
    @Query("SELECT d FROM FestivalDetailEntity d WHERE d.festival.festivalNo = :festivalNo")
    FestivalDetailEntity findByFestivalNo(@Param("festivalNo") Long festivalNo);

    @Modifying
    @Query("delete from FestivalDetailEntity d where d.festival.festivalNo = :festivalNo")
    void deleteByFestivalNo(Long festivalNo);
}
