package com.example.ex02.festival.controller;

import com.example.ex02.festival.service.FavoriteService;
import com.example.ex02.member.entity.MemberEntity;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
@RequestMapping("/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/toggle")
    public String toggleFavorite(@RequestParam Long festivalNo,
                                 @RequestParam String redirectUrl,
                                 HttpSession session) {

        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");

        // 로그인 안 되어 있으면 로그인 페이지로
        if (loginUser == null) {
            // 로그인 후 원래 페이지로 돌아감
            String encoded = URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8);
            return "redirect:/login?redirectUrl=" + encoded;
        }

        // 즐겨찾기 토글
        favoriteService.toggleFavorite(loginUser.getUserNo(), festivalNo);

        // 원래 페이지으로 리다이렉트
        return "redirect:" + redirectUrl;
    }
}
