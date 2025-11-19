//FestivalEntity
package com.example.ex02.festival.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "FESTIVAL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FestivalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "festival_seq_gen")
    @SequenceGenerator(
            name = "festival_seq_gen",
            sequenceName = "SEQ_FESTIVAL",
            allocationSize = 1
    )
    @Column(name = "FESTIVAL_NO")
    private Long festivalNo;

    @Column(name = "ADDR", length = 200)
    private String addr;

    @Column(name = "TITLE", length = 100, nullable = false)
    private String title;

    @Column(name = "MAPX")
    private Double mapx;

    @Column(name = "MAPY")
    private Double mapy;

    @Column(name = "EVENT_STARTDATE")
    private LocalDateTime eventStartDate;

    @Column(name = "EVENT_ENDDATE")
    private LocalDateTime eventEndDate;

    @Column(name = "PLAYTIME", length = 50)
    private String playtime;

    @Column(name = "IS_ACTIVE", length = 1)
    private String isActive; // 'Y', 'N'

    @Column(name = "STATE", length = 50, nullable = false)
    private String state;    // '예정' 등

	@OneToMany(mappedBy = "festival", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<FestivalImageEntity> images = new ArrayList<>();

	@OneToOne(mappedBy = "festival", cascade = CascadeType.ALL, orphanRemoval = true)
	private FestivalDetailEntity detail;

	@OneToMany(mappedBy = "festival", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("tagStrength DESC")
	private List<FestivalTagEntity> tags = new ArrayList<>();

	@OneToMany(mappedBy = "festival", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ReviewEntity> reviews = new ArrayList<>();

	// 평균 리뷰
	@Transient
	public double getAvgRating() {
		if (reviews == null || reviews.isEmpty()) {
			return 0.0;
		}

		return reviews.stream()
				.filter(r -> r.getRating() != null)
				.mapToDouble(ReviewEntity::getRating)
				.average()
				.orElse(0.0);
	}
}
