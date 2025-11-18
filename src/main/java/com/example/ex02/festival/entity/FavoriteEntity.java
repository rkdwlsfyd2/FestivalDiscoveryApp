// src/main/java/com/example/ex02/favorite/entity/FavoriteEntity.java
//FavoriteEntity
package com.example.ex02.festival.entity;

import java.time.LocalDateTime;

import com.example.ex02.member.entity.MemberEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @Column(name = "FAVORITE_DATE")
    private LocalDateTime favoriteDate;
}
