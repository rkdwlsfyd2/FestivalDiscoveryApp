package com.example.ex02.admin.dto;

import com.example.ex02.festival.entity.FestivalEntity;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FestivalSummaryDto {

    private Long festivalNo;
    private String title;
    private String state;          // 지역/시도 등
    private LocalDate eventStartDate;

    public static FestivalSummaryDto from(FestivalEntity entity) {
        return FestivalSummaryDto.builder()
                .festivalNo(entity.getFestivalNo())
                .title(entity.getTitle())
                .state(entity.getState())
                .eventStartDate(entity.getEventStartDate().toLocalDate())
                .build();
    }
}
