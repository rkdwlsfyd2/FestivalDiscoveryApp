package com.example.ex02.admin.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopFavoriteFestivalDto {
    private Long festivalNo;
    private String title;
    private Long favoriteCount;
}
