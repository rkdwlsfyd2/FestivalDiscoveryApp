package com.example.ex02.festival.dto;

import com.example.ex02.festival.entity.FestivalEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FestivalListDto {

    private Long festivalNo;
    private String title;
    private String addr;
    private Double mapx;
    private Double mapy;

    public static FestivalListDto fromEntity(FestivalEntity entity) {
        return FestivalListDto.builder()
                .festivalNo(entity.getFestivalNo())
                .title(entity.getTitle())
                .addr(entity.getAddr())
                .mapx(entity.getMapx())
                .mapy(entity.getMapy())
                .build();
    }
}
