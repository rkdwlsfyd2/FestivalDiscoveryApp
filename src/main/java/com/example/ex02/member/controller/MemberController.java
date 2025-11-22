package com.example.ex02.member.controller;

import com.example.ex02.member.dto.MemberDto;
import com.example.ex02.member.service.EmailService;
import com.example.ex02.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final EmailService emailService;

    // ================================
    // ⭐ 로그인 페이지
    // ================================
    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String signupSuccess,
                            @RequestParam(required = false) String redirectUrl,
                            Model model) {

        if ("true".equals(signupSuccess)) {
            model.addAttribute("signupSuccess", true);
        }
        model.addAttribute("redirectUrl", redirectUrl);
        return "member/login";
    }

    // ================================
    // ⭐ 로그인 처리
    // ================================
    @PostMapping("/login")
    public String login(@RequestParam String userId,
                        @RequestParam String password,
                        @RequestParam(required = false) String redirectUrl,
                        HttpSession session,
                        Model model) {

        var member = memberService.login(userId, password);

        if (member == null) {
            model.addAttribute("error", "아이디 또는 비밀번호가 잘못되었습니다.");
            return "member/login";
        }

        session.setAttribute("loginUser", member);
        if (redirectUrl != null && !redirectUrl.isBlank()) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/";
    }

    // ================================
    // ⭐ 회원가입 페이지
    // ================================
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("memberDto", new MemberDto());
        return "member/signup";
    }

    // ================================
    // ⭐ 회원가입 처리
    // ================================
    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute MemberDto memberDto,
                         BindingResult bindingResult,
                         Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("memberDto", memberDto);
            return "member/signup";
        }

        try {
            memberService.signup(memberDto);

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("memberDto", memberDto);
            return "member/signup";
        }

        return "redirect:/login?signupSuccess=true";
    }

    // ================================
    // ⭐ 마이페이지
    // ================================
    @GetMapping("/mypage")
    public String myPage() {
        return "member/mypage";
    }

    // ================================
    // ⭐ 아이디 찾기 페이지
    // ================================
    @GetMapping("/find-id")
    public String findIdPage() {
        return "member/find-id";
    }

    // ================================
    // ⭐ 비밀번호 찾기 페이지
    // ================================
    @GetMapping("/find-password")
    public String findPasswordPage() {
        return "member/find-password";
    }

    // ================================
    // ⭐ 비밀번호 재설정 페이지
    // ================================
    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "member/reset-password";
    }
    // 로그아웃 기능
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();  // 세션 전체 삭제 → 로그인 해제됨
        return "redirect:/";   // 로그아웃 후 메인으로 이동
    }

}
