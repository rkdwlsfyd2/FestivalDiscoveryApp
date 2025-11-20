package com.example.ex02.member.repository;

import com.example.ex02.member.entity.MemberAiScoreEntity;
import com.example.ex02.member.entity.MemberEntity;
import com.example.ex02.festival.entity.FestivalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberAiScoreRepository extends JpaRepository<MemberAiScoreEntity, Long> {

    List<MemberAiScoreEntity> findByMember(MemberEntity member);

    Optional<MemberAiScoreEntity> findByMemberAndFestival(MemberEntity member, FestivalEntity festival);
    // 특정 회원의 기존 점수 전부 삭제
    void deleteByMember(MemberEntity member);
}
