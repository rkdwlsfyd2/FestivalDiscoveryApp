//ReviewEntity
package com.example.ex02.festival.entity;

import com.example.ex02.member.entity.MemberEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "REVIEW")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "review_seq_gen")
    @SequenceGenerator(
            name = "review_seq_gen",
            sequenceName = "REVIEW_SEQ",
            allocationSize = 1
    )
    @Column(name = "REVIEW_NO")
    private Long reviewNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_NO", nullable = false)
    private MemberEntity member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FESTIVAL_NO", nullable = false)
    private FestivalEntity festival;

	@Column(name = "RATING")
	private Double rating;  // 0.0 ~ 5.0 (0.5 단위)

    @Column(name = "CONTENT", length = 1000)
    private String content;

	@CreationTimestamp
	@Column(name = "CREATED_AT", updatable = false)
	private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
}
