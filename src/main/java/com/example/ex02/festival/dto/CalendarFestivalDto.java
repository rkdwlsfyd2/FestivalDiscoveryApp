package com.example.ex02.festival.dto;

import com.example.ex02.festival.entity.FestivalEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

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
    private LocalDate date;                         // 날짜 (yyyy-MM-dd)
    private boolean inMonth;                        // 이번 달에 속하는지 여부
    private List<CalendarFestivalDto> festivals;    // 이 날 진행되는 축제 리스트

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
