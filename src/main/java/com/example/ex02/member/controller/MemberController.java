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
    // ⭐ 로그인 처리 (비활성 회원 체크 포함)
    // ================================
    @PostMapping("/login")
    public String login(@RequestParam String userId,
                        @RequestParam String password,
                        @RequestParam(required = false) String redirectUrl,
                        HttpSession session,
                        Model model) {

        var member = memberService.login(userId, password);

        // 로그인 실패
        if (member == null) {
            model.addAttribute("error", "아이디 또는 비밀번호가 잘못되었습니다.");
            return "member/login";
        }

        // ⭐ 비활성 회원 로그인 차단 (첫 번째 버전 유지)
        if ("INACTIVE".equals(member.getPassword())) {
            model.addAttribute("error", "비활성화된 계정입니다.");
            return "member/login";
        }

        // 정상 로그인
        session.setAttribute("loginUser", member);

        // redirectUrl 있으면 해당 페이지로
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

    // ================================
    // ⭐ 로그아웃
    // ================================
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

}
