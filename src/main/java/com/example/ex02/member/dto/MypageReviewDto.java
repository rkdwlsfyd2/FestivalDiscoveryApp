package com.example.ex02.member.dto;

import lombok.Data;

@Data
public class MypageReviewDto {
    private Long reviewNo;
    private String festivalTitle;
    private Double rating;
    private String content;
    private String createdAt;
    private String updatedAt;
    private Long festivalNo;
}
