package com.example.ex02.festival.service;

import com.example.ex02.festival.dto.CalendarFestivalDto;
import com.example.ex02.festival.entity.FestivalEntity;
import com.example.ex02.festival.repository.FestivalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final FestivalRepository festivalRepository;

    // year, month, region 기준으로 달력 데이터 조회
    public Map<LocalDate, List<CalendarFestivalDto>> getCalendar(int year, int month, String region) {

        YearMonth ym = YearMonth.of(year, month);
        LocalDate startOfMonth = ym.atDay(1);
        LocalDate endOfMonth = ym.atEndOfMonth();

        LocalDateTime startDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endDateTime = endOfMonth.atTime(23, 59, 59);

        // 1) 해당 월에 겹치는 축제 전체 조회
        List<FestivalEntity> festivals = festivalRepository.findFestivalsForMonth(
                startDateTime, endDateTime, region
        );

        // 2) 월 전체 날짜를 미리 Map으로 생성
        Map<LocalDate, List<CalendarFestivalDto>> calendarMap = new LinkedHashMap<>();
        LocalDate cursor = startOfMonth;
        while (!cursor.isAfter(endOfMonth)) {
            calendarMap.put(cursor, new ArrayList<>());
            cursor = cursor.plusDays(1);
        }

        // 3) 각 축제를 날짜별로 쪼개서 넣기
        for (FestivalEntity festival : festivals) {
            CalendarFestivalDto dto = CalendarFestivalDto.from(festival);

            LocalDate festivalStart = festival.getEventStartDate().toLocalDate();
            LocalDate festivalEnd = festival.getEventEndDate().toLocalDate();

            LocalDate applyStart = festivalStart.isBefore(startOfMonth) ? startOfMonth : festivalStart;
            LocalDate applyEnd = festivalEnd.isAfter(endOfMonth) ? endOfMonth : festivalEnd;

            LocalDate day = applyStart;
            while (!day.isAfter(applyEnd)) {
                List<CalendarFestivalDto> list = calendarMap.get(day);
                if (list != null) {
                    list.add(dto);
                }
                day = day.plusDays(1);
            }
        }

        return calendarMap;
    }
}
