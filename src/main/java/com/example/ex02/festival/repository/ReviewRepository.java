package com.example.ex02.festival.repository;

import com.example.ex02.festival.entity.FestivalEntity;
import com.example.ex02.festival.entity.ReviewEntity;
import com.example.ex02.member.entity.MemberEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    /*  관리자 리뷰 목록 전용 검색 + 페이징 + 정렬  */
    @Query("""
        SELECT r FROM ReviewEntity r
        JOIN r.member m
        JOIN r.festival f
        WHERE (:keyword IS NULL OR :keyword = '' 
               OR m.userId LIKE %:keyword% 
               OR r.content LIKE %:keyword%)
        """)
    Page<ReviewEntity> searchReviews(@Param("keyword") String keyword,
                                     Pageable pageable);

    @Modifying
    @Query("delete from ReviewEntity r where r.festival.festivalNo = :festivalNo")
    void deleteByFestivalFestivalNo(Long festivalNo);

    // 전체 평균 평점
    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM ReviewEntity r")
    Double findAverageRating();

    @Query("""
           SELECT COALESCE(AVG(r.rating), 0)
           FROM ReviewEntity r
           WHERE r.createdAt >= :start
             AND r.createdAt <  :end
           """)
    Double findAverageScoreBetween(@Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);

    // 오늘 작성된 리뷰 수
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    Page<ReviewEntity> findByMember_UserNo(Long userNo, Pageable pageable);

}
