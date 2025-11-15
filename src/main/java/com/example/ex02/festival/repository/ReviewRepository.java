package com.example.ex02.festival.repository;

import com.example.ex02.festival.entity.FestivalEntity;
import com.example.ex02.festival.entity.ReviewEntity;
import com.example.ex02.member.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {

    List<ReviewEntity> findByFestival(FestivalEntity festival);

    List<ReviewEntity> findByMember(MemberEntity member);
}
