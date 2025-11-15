package com.example.ex02.festival.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "FESTIVAL_DETAIL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FestivalDetailEntity {

    @Id
    @Column(name = "FESTIVAL_NO")
    private Long festivalNo;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "FESTIVAL_NO")
    private FestivalEntity festival;

    @Lob
    @Column(name = "INFOTEXT_1")
    private String infotext1;

    @Lob
    @Column(name = "INFOTEXT_2")
    private String infotext2;

    @Column(name = "HOST", length = 200)
    private String host;

    @Column(name = "HOST_TEL", length = 50)
    private String hostTel;

    @Column(name = "HOMEPAGE", length = 300)
    private String homepage;

    @Column(name = "FESTIVAL_FEE", length = 200)
    private String festivalFee;
}
