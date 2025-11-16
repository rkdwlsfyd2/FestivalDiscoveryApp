package com.example.ex02.festival.dto;

import com.example.ex02.festival.entity.FestivalEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class CalendarFestivalDto {

    private Long festivalNo;
    private String title;
    private String addr;
    private LocalDate startDate;
    private LocalDate endDate;
    private String playtime;
    private String state;

    public static CalendarFestivalDto from(FestivalEntity entity) {
        return CalendarFestivalDto.builder()
                .festivalNo(entity.getFestivalNo())
                .title(entity.getTitle())
                .addr(entity.getAddr())
                .startDate(entity.getEventStartDate().toLocalDate()) // LocalDateTime -> LocalDate
                .endDate(entity.getEventEndDate().toLocalDate())
                .playtime(entity.getPlaytime())
                .state(entity.getState())
                .build();
    }
}
