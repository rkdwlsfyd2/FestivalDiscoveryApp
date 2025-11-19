package com.example.ex02.festival.controller;

import com.example.ex02.festival.service.FestivalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/festivals")
@RequiredArgsConstructor
public class FestivalController {

    private final FestivalService festivalService;

    @GetMapping
    public String list(Model model) {
        return "festival/list-map";
    }

    @GetMapping("/{festivalNo}")
    public String detail(@PathVariable Long festivalNo, Model model) {
        return "festival/detail";
    }

    @GetMapping("/calendar")
    public String calendar(Model model) {
        // TODO: 캘린더 데이터 세팅
        return "festival/calendar";
    }
    // 디자인 표본 페이지
    // http://localhost:9898/festivals/designConfig
    @GetMapping("/designConfig")
    public String designConfigPage() {
        return "review/designConfig";
    }

    // 리뷰 페이지
    // http://localhost:9898/festivals/review
    @GetMapping("/review")
    public String reviewPage() {
        return "review/review_integration";
    }
}
