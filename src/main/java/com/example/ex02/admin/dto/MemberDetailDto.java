package com.example.ex02.admin.dto;

import com.example.ex02.member.entity.MemberEntity;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDetailDto {

    private MemberEntity member;                  // 기본 정보

    private List<ReviewSummaryDto> reviews;       // 작성한 리뷰
    private List<FavoriteFestivalDto> favorites;  // 즐겨찾기 축제
}