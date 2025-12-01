package com.example.ex02.admin.dto;

import com.example.ex02.member.entity.MemberEntity;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberSummaryDto {
    private Long   userNo;
    private String name;
    private String email;
    private LocalDate joinDate;
    private LocalDateTime lastUpdated;
    private String status;       // ACTIVE / SUSPENDED / WITHDRAWN
    private String statusLabel;  // "정상", "정지", "탈퇴" 등

    public static MemberSummaryDto from(MemberEntity entity) {
        return MemberSummaryDto.builder()
                .userNo(entity.getUserNo())
                .name(entity.getName())
                .email(entity.getEmail())
                .joinDate(entity.getJoinDate())
                .build();
    }
}