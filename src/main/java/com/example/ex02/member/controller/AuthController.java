package com.example.ex02.member.controller;

import com.example.ex02.member.dto.MemberDto;
import com.example.ex02.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;

    @GetMapping("/login")
    public String loginForm() {
        return "member/login";
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        return "member/signup";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute MemberDto memberDto) {
        return "redirect:/login";
    }
}
