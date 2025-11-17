package com.example.ex02.member.controller;

import com.example.ex02.member.entity.MemberEntity;
import com.example.ex02.member.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class TestLoginController {

    private final MemberRepository memberRepository;

    // 로그인 폼
    @GetMapping("/test-login")
    public String testLoginForm() {
        return "member/test-login";   // /templates/member/test-login.html
    }

    // 로그인 처리 (평문 비번)
    @PostMapping("/test-login")
    public String testLogin(@RequestParam String userId,
                            @RequestParam String Password,
                            HttpSession session,
                            Model model) {

        Optional<MemberEntity> optionalMember =
                memberRepository.findByUserIdAndPassword(userId, Password);

        if (optionalMember.isEmpty()) {
            model.addAttribute("loginError", "아이디 또는 비밀번호가 올바르지 않습니다.");
            return "member/test-login";
        }

        MemberEntity member = optionalMember.get();

        // ★ 세션에 로그인 사용자 번호 저장 (우리가 계속 쓰던 키)
        session.setAttribute("loginUserNo", member.getUserNo());
        session.setAttribute("loginUserName", member.getName()); // 필요하면

        // 로그인 이후 어디로 보낼지 – 캘린더로 바로 가도록
        return "redirect:/calendar";
    }

    // 테스트용 로그아웃
    @GetMapping("/test-logout")
    public String testLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/test-login";
    }
}
