// com.example.ex02.admin.dto.FavoriteFestivalDto
package com.example.ex02.admin.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteFestivalDto {

    private Long festivalNo;
    private String title;
    private String addr;
    private long   favoriteCount;
}
