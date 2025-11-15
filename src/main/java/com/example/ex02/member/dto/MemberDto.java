package com.example.ex02.member.dto;

import com.example.ex02.member.entity.MemberEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {

    private Long userNo;
    private String userId;
    private String password;
    private String name;
    private String email;

//    public static MemberDto fromEntity(MemberEntity entity) {
//        return MemberDto.builder()
//                .userNo(entity.getUserNo())
//                .userId(entity.getUserId())
//                .name(entity.getName())
//                .email(entity.getEmail())
//                .build();
//    }
}
