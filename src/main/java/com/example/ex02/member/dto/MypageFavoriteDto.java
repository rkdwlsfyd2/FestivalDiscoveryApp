package com.example.ex02.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MypageFavoriteDto {
    private Long favoriteNo;
    private Long festivalNo;
    private String title;
    private String state;
    private String playtime;
    private String imageUrl;  // 이미지 URL
    private LocalDateTime favoriteDate;
}