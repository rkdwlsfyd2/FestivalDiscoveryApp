package com.example.ex02.member.entity;

import com.example.ex02.festival.entity.FestivalEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "MEMBER_AI_SCORE",
        uniqueConstraints = {
                @UniqueConstraint(name = "UQ_AI_USER_FESTIVAL", columnNames = {"USER_NO", "FESTIVAL_NO"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberAiScoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ai_score_seq_gen")
    @SequenceGenerator(
            name = "ai_score_seq_gen",
            sequenceName = "SEQ_AI_SCORE",
            allocationSize = 1
    )
    @Column(name = "AI_SCORE_NO")
    private Long aiScoreNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_NO", nullable = false)
    private MemberEntity member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FESTIVAL_NO", nullable = false)
    private FestivalEntity festival;

    @Column(name = "AI_SCORE")
    private Double aiScore;  // 0 ~ 1

    @Column(name = "SCORED_AT")
    private LocalDateTime scoredAt;
}
