// src/main/java/com/example/ex02/favorite/entity/FavoriteEntity.java
package com.example.ex02.festival.entity;

import com.example.ex02.festival.entity.FestivalEntity;
import com.example.ex02.member.entity.MemberEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "FAVORITE",
        uniqueConstraints = {
                @UniqueConstraint(name = "UQ_USER_FESTIVAL", columnNames = {"USER_NO", "FESTIVAL_NO"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "favorite_seq_gen")
    @SequenceGenerator(
            name = "favorite_seq_gen",
            sequenceName = "SEQ_FAVORITE",
            allocationSize = 1
    )
    @Column(name = "FAVORITE_NO")
    private Long favoriteNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_NO", nullable = false)
    private MemberEntity member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FESTIVAL_NO", nullable = false)
    private FestivalEntity festival;
    private String imageUrl;
    @Column(name = "FAVORITE_DATE")
    private LocalDateTime favoriteDate;
}
