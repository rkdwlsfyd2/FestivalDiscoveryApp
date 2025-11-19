//FestivalImageEntity
package com.example.ex02.festival.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "FESTIVAL_IMAGE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FestivalImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "image_seq_gen")
    @SequenceGenerator(
            name = "image_seq_gen",
            sequenceName = "SEQ_IMAGE",
            allocationSize = 1
    )
    @Column(name = "IMAGE_NO")
    private Long imageNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FESTIVAL_NO", nullable = false)
    private FestivalEntity festival;

    @Column(name = "IMAGE_URL", length = 100, nullable = false)
    private String imageUrl;
}
