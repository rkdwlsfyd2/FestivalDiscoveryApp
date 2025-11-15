package com.example.ex02.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberController {

    @GetMapping("/mypage")
    public String myPage() {
        // TODO: 로그인 회원 정보, 즐겨찾기, 리뷰 등 불러오기
        return "member/mypage";
    }
}
