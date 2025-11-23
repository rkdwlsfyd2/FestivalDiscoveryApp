package com.example.ex02.admin.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSummaryDto {

    private Long reviewId;
    private Long festivalNo;
    private String festivalTitle;
    private String content;
    private Double rating;
    private LocalDateTime createdAt;
}
