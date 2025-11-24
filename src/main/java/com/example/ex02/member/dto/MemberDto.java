package com.example.ex02.member.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {

    private Long userNo;

    // ⭐ 아이디: 영문 + 숫자, 5~20자
    @NotBlank(message = "아이디는 필수 입력입니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]{5,20}$",
            message = "아이디는 영문/숫자 조합 5~20자여야 합니다.")
    private String userId;

    // ⭐ 비밀번호: 8~20자, 영문 + (숫자 또는 특수문자)
    @NotBlank(message = "비밀번호는 필수 입력입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d|.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/]).{8,20}$",
            message = "비밀번호는 8~20자, 영문 + 숫자 또는 특수문자를 포함해야 합니다."
    )
    private String password;

    @NotBlank(message = "이름은 필수 입력입니다.")
    private String name;

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일은 필수 입력입니다.")
    private String email;

    // 이메일 인증 코드
    private String emailCode;

    private String phone;

    private String gender;

    private LocalDate birthDate;

    private LocalDate joinDate;

    private String role;

    private String isActive;

    private LocalDate withdrawDate;

    // ⭐ 선호 태그 필수
    @NotBlank(message = "선호 태그는 필수 선택입니다.")
    private String favoriteTag;

    public String getFormattedPhone() {
        if (phone == null) return "";
        // 숫자만 추출
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() == 11) {
            return digits.replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
        }
        return phone; // 예상치 못한 케이스는 원본 출력
    }

}
