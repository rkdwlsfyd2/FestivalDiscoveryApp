package com.example.ex02.member.repository;

import com.example.ex02.festival.entity.FestivalEntity;
import com.example.ex02.member.entity.MemberAiScoreEntity;
import com.example.ex02.member.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberAiScoreRepository extends JpaRepository<MemberAiScoreEntity, Long> {

    List<MemberAiScoreEntity> findByMember(MemberEntity member);

    Optional<MemberAiScoreEntity> findByMemberAndFestival(MemberEntity member, FestivalEntity festival);
    // 특정 회원의 기존 점수 전부 삭제
    void deleteByMember(MemberEntity member);
    // 추가 특정 회원의 기존 점수 전부 삭제 (user_no 기준으로 강제 삭제)
    @Modifying
    @Query("DELETE FROM MemberAiScoreEntity s WHERE s.member.userNo = :userNo")
    void deleteByUserNo(@Param("userNo") Long userNo);

    @Query("""
	       SELECT s
	         FROM MemberAiScoreEntity s
	        WHERE s.member = :member
	          AND s.festival.festivalNo IN :festivalNos
	        ORDER BY s.aiScore DESC, s.scoredAt DESC
	       """)
    List<MemberAiScoreEntity> findByMemberAndFestivalNosOrderByScoreDesc(
            @Param("member") MemberEntity member,
            @Param("festivalNos") Collection<Long> festivalNos
    );


}
