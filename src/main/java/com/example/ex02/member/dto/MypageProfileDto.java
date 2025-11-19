package com.example.ex02.member.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class MypageProfileDto {
    private Long userNo;
    private String userId;
    private String name;
    private String email;
    private String phone;
    private String gender;
    private LocalDate birthDate;
    private LocalDate joinDate;
    private String favoriteTag;
}
