/*MypageAccountDto*/
package com.example.ex02.member.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MypageAccountDto {
    private Long userNo;
    private String userId;
    private String name;
    private String email;
    private String phone;
    private String password; // 수정할 때만
    private String favoriteTag;
}
