package com.example.ex02.admin.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UserStatsDto {
    private List<String> ageLabels;
    private List<Long> ageCounts;

    private List<String> genderLabels;
    private List<Long> genderCounts;

    private List<String> tagLabels;
    private List<Long> tagCounts;
}
