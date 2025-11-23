package com.example.ex02.festival.repository;

import com.example.ex02.festival.entity.FestivalEntity;
import com.example.ex02.festival.entity.ReviewEntity;
import com.example.ex02.member.entity.MemberEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {

    List<ReviewEntity> findByFestival(FestivalEntity festival);

    List<ReviewEntity> findByMember(MemberEntity member);
    List<ReviewEntity> findByMember_UserNo(Long userNo);

	Page<ReviewEntity> findByFestival_FestivalNo(Long festivalNo, Pageable pageable);

	boolean existsByFestival_FestivalNoAndMember_UserNo(Long festivalNo, Long userNo);

    @Query("select r from ReviewEntity r " +
            "join fetch r.festival f " +
            "where r.member.userNo = :userNo")
    List<ReviewEntity> findByMemberUserNoWithFestival(@Param("userNo") Long userNo);
}
