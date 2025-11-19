package com.example.ex02.member.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // 이메일 인증 코드 저장
    private Map<String, String> verificationCodes = new HashMap<>();

    // ================================
    // ⭐ 회원가입 인증코드 (TEXT 메일)
    // ================================
    public String sendVerificationCode(String email) {

        String code = String.valueOf((int)(Math.random() * 900000) + 100000);
        verificationCodes.put(email, code);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("회원가입 인증 코드");
            message.setText("인증코드: " + code);

            mailSender.send(message);

            System.out.println("📧 인증코드 발송 완료 → " + email);
            return code;

        } catch (Exception e) {
            System.out.println("❌ 인증코드 발송 실패: " + e.getMessage());
            return null;
        }
    }

    // 인증 코드 검증
    public boolean verifyCode(String email, String code) {
        return code.equals(verificationCodes.get(email));
    }

    // ================================
    // ⭐ 비밀번호 재설정 HTML 메일
    // ================================
    public void sendHtmlMail(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom("wjdendnjs1@gmail.com");
            helper.setSubject(subject);
            helper.setText(htmlContent, true);  // ⭐ HTML 적용

            mailSender.send(mimeMessage);

            System.out.println("📨 HTML 메일 발송 완료 → " + to);

        } catch (Exception e) {
            System.out.println("❌ HTML 메일 발송 실패: " + e.getMessage());
        }
    }

    // ================================
    // ⭐ 일반 TEXT 메일 (예비)
    // ================================
    public void sendMail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom("wjdendnjs1@gmail.com");
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);

            System.out.println("📨 기본 메일 발송 완료 → " + to);

        } catch (Exception e) {
            System.out.println("❌ 기본 메일 발송 실패: " + e.getMessage());
        }
    }
}
