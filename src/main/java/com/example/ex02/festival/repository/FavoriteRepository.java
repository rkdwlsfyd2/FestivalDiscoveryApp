// src/main/java/com/example/ex02/favorite/repository/FavoriteRepository.java
package com.example.ex02.festival.repository;

import com.example.ex02.festival.entity.FavoriteEntity;
import com.example.ex02.festival.entity.FestivalEntity;
import com.example.ex02.member.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<FavoriteEntity, Long> {

    List<FavoriteEntity> findByMember(MemberEntity member);

    Optional<FavoriteEntity> findByMemberAndFestival(MemberEntity member, FestivalEntity festival);

    boolean existsByMemberAndFestival(MemberEntity member, FestivalEntity festival);
}
