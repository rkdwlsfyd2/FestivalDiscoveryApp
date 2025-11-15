package com.example.ex02.admin.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardDto {

    private long memberCount;
    private long festivalCount;
    private long reviewCount;
}
