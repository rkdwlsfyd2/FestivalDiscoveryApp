package com.example.ex02.member.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MypageReviewDto {
    private Long reviewNo;
    private String festivalTitle;
    private Double rating;
    private String content;
    private LocalDateTime  createdAt;
    private LocalDateTime updatedAt;
    private Long festivalNo;
}
