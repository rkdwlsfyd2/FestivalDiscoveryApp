package com.example.ex02.member.repository;

import com.example.ex02.member.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    Optional<MemberEntity> findByUserId(String userId);
    boolean existsByUserId(String userId);
    boolean existsByEmail(String email);
    Optional<MemberEntity> findByEmail(String email);


    @Query(value = """
        SELECT 
            (TRUNC((EXTRACT(YEAR FROM SYSDATE) - to_char(m.birth_date, 'yyyy')) / 10) * 10) AS age_group,
            COUNT(*) AS cnt
        FROM member m
        WHERE m.is_active = 'Y'
        GROUP BY (TRUNC((EXTRACT(YEAR FROM SYSDATE) - to_char(m.birth_date, 'yyyy')) / 10) * 10)
        ORDER BY age_group
        """, nativeQuery = true)
    List<Object[]> countMembersByAgeGroup();


    @Query(value = """
        SELECT 
            m.gender AS gender,
            COUNT(*) AS cnt
        FROM member m
        WHERE m.is_active = 'Y'
        GROUP BY m.gender
        """, nativeQuery = true)
    List<Object[]> countMembersByGender();

}
