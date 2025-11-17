package com.example.ex02.festival.controller;

import com.example.ex02.festival.dto.CalendarFestivalDto;
import com.example.ex02.festival.service.CalendarService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Collections;
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
                           HttpSession session, // todo
                           Model model) {

        LocalDate today = LocalDate.now();
        int targetYear = (year != null) ? year : today.getYear();
        int targetMonth = (month != null) ? month : today.getMonthValue();

        // 세션에서 로그인한 회원 번호 가져오기 (로그인 안 했으면 null)
        Long memberNo = (Long) session.getAttribute("loginUserNo");

        Map<LocalDate, List<CalendarFestivalDto>> calendarMap =
                calendarService.getCalendar(targetYear, targetMonth, region, memberNo);

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

        // 지역 목록 (임시 하드코딩)
        List<String> regions = List.of("서울","경기","강원","충남","전북","전남","부산","경북","경남","인천","충북","제주","대구","대전","광주","울산","세종");
        model.addAttribute("regions", regions);

        // 주(week) 단위 달력 데이터
        List<List<CalendarFestivalDto>> weeks = calendarService.buildCalendar(targetYear, targetMonth, region, memberNo);
        model.addAttribute("weeks", weeks);

        List<CalendarFestivalDto> favoriteFestivals =
                calendarService.getMonthlyFavorites(targetYear, targetMonth, region, memberNo);
        if(favoriteFestivals == null){
            favoriteFestivals = Collections.emptyList();
        }
        model.addAttribute("favoriteFestivals", favoriteFestivals);

        return "festival/calendar";
    }
}
