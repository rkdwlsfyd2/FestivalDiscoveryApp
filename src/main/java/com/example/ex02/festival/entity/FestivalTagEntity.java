//FestivalTagEntity
package com.example.ex02.festival.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "FESTIVAL_TAG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FestivalTagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tag_seq_gen")
    @SequenceGenerator(
            name = "tag_seq_gen",
            sequenceName = "SEQ_TAG",
            allocationSize = 1
    )
    @Column(name = "FESTIVAL_TAG_NO")
    private Long festivalTagNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FESTIVAL_NO", nullable = false)
    private FestivalEntity festival;

    @Column(name = "TAG", length = 30, nullable = false)
    private String tag;

	@Column(name = "TAG_STRENGTH", nullable = false)
	private Double tagStrength;
}
