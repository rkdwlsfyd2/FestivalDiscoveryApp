package com.example.ex02.festival.controller;

import com.example.ex02.festival.dto.CalendarFestivalDto;
import com.example.ex02.festival.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/calendar")
    public String calendar(@RequestParam(required = false) Integer year,
                           @RequestParam(required = false) Integer month,
                           @RequestParam(required = false) String region,
                           Model model) {

        LocalDate today = LocalDate.now();
        int targetYear = (year != null) ? year : today.getYear();
        int targetMonth = (month != null) ? month : today.getMonthValue();

        Map<LocalDate, List<CalendarFestivalDto>> calendarMap =
                calendarService.getCalendar(targetYear, targetMonth, region);

        model.addAttribute("calendarMap", calendarMap);
        model.addAttribute("year", targetYear);
        model.addAttribute("month", targetMonth);
        model.addAttribute("selectedRegion", region);

        // 이전/다음달 계산해서 버튼에 사용
        LocalDate current = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate prev = current.minusMonths(1);
        LocalDate next = current.plusMonths(1);

        model.addAttribute("prevYear", prev.getYear());
        model.addAttribute("prevMonth", prev.getMonthValue());
        model.addAttribute("nextYear", next.getYear());
        model.addAttribute("nextMonth", next.getMonthValue());

        // 지역 목록 (select option)
//        model.addAttribute("regions", regionService.getRegions());
//
        // 주(week) 단위 달력 데이터
        List<List<CalendarFestivalDto>> weeks = calendarService.buildCalendar(targetYear, targetMonth, region);
        model.addAttribute("weeks", weeks);

        return "festival/calendar";
    }
}
