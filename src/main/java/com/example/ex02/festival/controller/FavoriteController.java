package com.example.ex02.festival.controller;

import com.example.ex02.festival.service.FavoriteService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/toggle")
    public String toggleFavorite(@RequestParam Long festivalNo,
                                 @RequestParam String redirectUrl,
                                 HttpSession session) {

        // 세션에서 로그인한 회원 번호 꺼내기
        Long memberNo = (Long) session.getAttribute("loginUserNo");

        // 로그인 안 되어 있으면 로그인 페이지로
        if (memberNo == null) {
            return "redirect:/test-login";   // 실제 로그인 URL에 맞게 수정
        }

        // 즐겨찾기 토글
        favoriteService.toggleFavorite(memberNo, festivalNo);

        // 다시 달력(or 원래 페이지)으로 리다이렉트
        return "redirect:" + redirectUrl;
    }
}
