package com.example.ex02.member.service;

import com.example.ex02.member.dto.MemberDto;
import com.example.ex02.member.entity.MemberEntity;
import com.example.ex02.member.entity.PasswordResetToken;
import com.example.ex02.member.repository.MemberRepository;
import com.example.ex02.member.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder encoder;
    private final EmailService emailService;
    private final PasswordResetTokenRepository tokenRepository;

    // ============================
    // ⭐ 회원가입 (최종 완성본)
    // ============================
    public MemberEntity signup(MemberDto dto) {

        // 1) 필수 입력 검증
        if (dto.getUserId() == null || dto.getUserId().trim().isEmpty())
            throw new IllegalArgumentException("아이디는 필수 입력입니다.");

        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty())
            throw new IllegalArgumentException("비밀번호는 필수 입력입니다.");

        if (dto.getName() == null || dto.getName().trim().isEmpty())
            throw new IllegalArgumentException("이름은 필수 입력입니다.");

        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty())
            throw new IllegalArgumentException("이메일은 필수 입력입니다.");

        if (dto.getFavoriteTag() == null || dto.getFavoriteTag().trim().isEmpty())
            throw new IllegalArgumentException("선호 태그는 필수 선택입니다.");

        // 2) 이메일 인증 코드 검증
        if (dto.getEmailCode() == null || dto.getEmailCode().trim().isEmpty())
            throw new IllegalArgumentException("이메일 인증을 완료해주세요.");

        boolean verified = emailService.verifyCode(dto.getEmail(), dto.getEmailCode());
        if (!verified)
            throw new IllegalArgumentException("이메일 인증 코드가 올바르지 않습니다.");

        // 3) 중복 체크
        if (memberRepository.existsByUserId(dto.getUserId()))
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");

        if (memberRepository.existsByEmail(dto.getEmail()))
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");

        // 4) 비밀번호 암호화
        String encryptedPassword = encoder.encode(dto.getPassword());

        // 5) Entity 생성 + 저장
        MemberEntity entity = MemberEntity.builder()
                .userId(dto.getUserId())
                .password(encryptedPassword)
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .gender(dto.getGender())
                .birthDate(dto.getBirthDate())
                .favoriteTag(dto.getFavoriteTag())
                .joinDate(LocalDate.now())  // DB 기본값과 동일하게 설정
                .role("user")
                .isActive("Y")
                .withdrawDate(null)
                .build();

        return memberRepository.save(entity);
    }


    // ============================
    // ⭐ 아이디 중복 체크
    // ============================
    public boolean checkUserId(String userId) {
        return memberRepository.existsByUserId(userId);
    }

    // ============================
    // ⭐ 이메일 중복 체크
    // ============================
    public boolean checkEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    // ============================
    // ⭐ 로그인
    // ============================
    public MemberEntity login(String userId, String rawPassword) {

        MemberEntity user = memberRepository.findByUserId(userId).orElse(null);

        if (user == null) return null;
        if (!encoder.matches(rawPassword, user.getPassword())) return null;
        if (!"Y".equals(user.getIsActive())) return null;

        return user;
    }

    // ============================
    // ⭐ 비밀번호 재설정 토큰 생성
    // ============================
    public String createResetToken(String email) {

        MemberEntity member = memberRepository.findByEmail(email).orElse(null);
        if (member == null) return null;

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .email(email)
                .token(token)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();

        tokenRepository.save(resetToken);

        return token;
    }

    // ============================
    // ⭐ 비밀번호 변경
    // ============================
    public boolean resetPassword(String token, String newPassword) {

        PasswordResetToken resetToken =
                tokenRepository.findByToken(token).orElse(null);

        if (resetToken == null) return false;

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now()))
            return false;

        MemberEntity member =
                memberRepository.findByEmail(resetToken.getEmail())
                        .orElse(null);

        if (member == null) return false;

        member.setPassword(encoder.encode(newPassword)); //해싱된 비번
//        member.setPassword(newPassword);  // 암호화 제거 (임시!)

        memberRepository.save(member);

        return true;
    }

    // ============================
//  마이페이지 회원 정보 수정
// ============================
    public boolean updateAccount(Long userNo, String name, String email, String phone,
                                 String favoriteTag, String newPassword) {

        MemberEntity member = memberRepository.findById(userNo).orElse(null);
        if (member == null) return false;

        // 이름/이메일/전화번호/태그 업데이트
        member.setName(name);
        member.setEmail(email);
        member.setPhone(phone);
        member.setFavoriteTag(favoriteTag);

        //  비밀번호 변경 시 반드시 암호화
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            String encodedPw = encoder.encode(newPassword);
            member.setPassword(encodedPw);   // 암호화된 비밀번호 저장
        }

        memberRepository.save(member);
        return true;
    }




}
