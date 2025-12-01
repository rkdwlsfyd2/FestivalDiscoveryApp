package com.example.ex02.festival.controller;

import com.example.ex02.festival.service.FavoriteService;
import com.example.ex02.member.entity.MemberEntity;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
@RequestMapping("/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/toggle")
    @ResponseBody
    public String toggleFavoriteAjax(
            @RequestParam Long festivalNo,
            HttpSession session) {

        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");

        // 로그인 안 되어 있는 경우
        if (loginUser == null) {
            return "NOT_LOGIN";
        }

        boolean added = favoriteService.toggleFavorite(loginUser.getUserNo(), festivalNo);

        return added ? "added" : "removed";
    }

}
