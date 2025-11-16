package com.example.ex02.festival.controller;

import com.example.ex02.festival.dto.CalendarFestivalDto;
import com.example.ex02.festival.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
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

        Map<LocalDate, java.util.List<CalendarFestivalDto>> calendarMap =
                calendarService.getCalendar(targetYear, targetMonth, region);

        model.addAttribute("calendarMap", calendarMap);
        model.addAttribute("year", targetYear);
        model.addAttribute("month", targetMonth);
        model.addAttribute("region", region);

        return "festival/calendar";
    }
}
