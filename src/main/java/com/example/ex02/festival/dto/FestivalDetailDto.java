// src/main/java/com/example/ex02/festival/dto/FestivalDetailDto.java
package com.example.ex02.festival.dto;

import com.example.ex02.festival.entity.FestivalDetailEntity;
import com.example.ex02.festival.entity.FestivalEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FestivalDetailDto {

    private Long festivalNo;
    private String title;
    private String addr;
    private String playtime;
    private String infotext1;
    private String infotext2;
    private String host;
    private String hostTel;
    private String homepage;
    private String festivalFee;

    public static FestivalDetailDto fromEntities(FestivalEntity f, FestivalDetailEntity d) {
        return FestivalDetailDto.builder()
                .festivalNo(f.getFestivalNo())
                .title(f.getTitle())
                .addr(f.getAddr())
                .playtime(f.getPlaytime())
                .infotext1(d != null ? d.getInfotext1() : null)
                .infotext2(d != null ? d.getInfotext2() : null)
                .host(d != null ? d.getHost() : null)
                .hostTel(d != null ? d.getHostTel() : null)
                .homepage(d != null ? d.getHomepage() : null)
                .festivalFee(d != null ? d.getFestivalFee() : null)
                .build();
    }
}
