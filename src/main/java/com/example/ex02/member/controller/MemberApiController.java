package com.example.ex02.member.controller;

import com.example.ex02.member.repository.MemberRepository;
import com.example.ex02.member.service.EmailService;
import com.example.ex02.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberApiController {

    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final MemberService memberService;   // ⭐ 필수 추가

    /** 아이디 중복 체크 (공란 포함) */
    @GetMapping("/check-userid")
    public Map<String, Boolean> checkUserId(@RequestParam String userId) {

        if (userId == null || userId.trim().isEmpty()) {
            return Map.of("exists", true);
        }

        boolean exists = memberRepository.existsByUserId(userId);
        return Map.of("exists", exists);
    }

    /** 이메일 중복 체크 (공란 포함) */
    @GetMapping("/check-email")
    public Map<String, Boolean> checkEmail(@RequestParam String email) {

        if (email == null || email.trim().isEmpty()) {
            return Map.of("exists", true);
        }

        boolean exists = memberRepository.existsByEmail(email);
        return Map.of("exists", exists);
    }

    /** 이메일 인증 코드 발송 (가입된 이메일인지 확인 포함) */
    @GetMapping("/send-email-code")
    public String sendEmailCode(@RequestParam String email,
                                @RequestParam(required = false) String type) {

        if (email == null || email.trim().isEmpty()) {
            return "EMPTY";
        }

        // 🎯 default: 회원가입
        if (type == null || type.equals("signup")) {
            emailService.sendVerificationCode(email);
            return "OK";
        }

        // 🎯 아이디 찾기 전용
        if (type.equals("findId")) {
            // 가입된 이메일인지 먼저 확인
            if (!memberRepository.existsByEmail(email)) {
                return "NOT_FOUND";
            }

            emailService.sendVerificationCodeForFindId(email);
            return "OK";
        }

        return "INVALID_TYPE";
    }


    /** 이메일 인증 코드 검증 */
    @PostMapping("/verify-email-code")
    public boolean verifyEmailCode(@RequestParam String email,
                                   @RequestParam String code) {
        return emailService.verifyCode(email, code);
    }

    /** 이메일로 아이디 찾기 */
    @GetMapping("/find-id")
    public String findUserId(@RequestParam String email) {

        return memberRepository.findByEmail(email)
                .map(member -> member.getUserId())
                .orElse("NOT_FOUND");
    }

    @GetMapping("/find-password/send")
    public String sendResetLink(@RequestParam String email) {

        String token = memberService.createResetToken(email);
        if (token == null) return "NOT_FOUND";

        String resetUrl = "https://3.85.93.179:9898/reset-password?token=" + token;

        // ⭐ HTML 이메일 내용
        String html = """
        <h2>비밀번호 재설정 안내</h2>
        <p>아래 버튼을 눌러 비밀번호를 다시 설정하세요.</p>
        <a href="%s" style="display:inline-block;
           padding:10px 20px; background:#4CAF50; color:white;
           text-decoration:none; border-radius:5px;">
           비밀번호 재설정하기
        </a>
        <p style="margin-top:20px;">
            ※ 만약 버튼이 안되면 아래 주소를 복사하세요.<br>
            %s
        </p>
    """.formatted(resetUrl, resetUrl);

        // ⭐ HTML 메일로 보내기
        emailService.sendHtmlMail(
                email,
                "비밀번호 재설정 안내",
                html
        );

        return "OK";
    }

    /** 비밀번호 재설정 처리 */
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String newPassword) {

        boolean result = memberService.resetPassword(token, newPassword);
        return result ? "OK" : "FAIL";
    }

}
