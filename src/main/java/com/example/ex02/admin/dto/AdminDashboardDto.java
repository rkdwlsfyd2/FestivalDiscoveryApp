package com.example.ex02.admin.dto;

import com.example.ex02.member.entity.MemberEntity;
import com.example.ex02.festival.entity.FestivalEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardDto {

    // 공통
    private LocalDateTime lastUpdated;

    // 상단 4개 카드
    private long totalMembers;
    private long todayJoined;
    private long totalFestivals;
    private long totalReviews;
    private long totalFavorites;

    private long todaySignups;
    private long weekSignups;
    private double memberGrowthRate;        // 지난달 대비 % (ex. 12.3, -3.2)

    private long activeFestivals;           // 진행중인 축제 수
    private long thisMonthFestivals;
    private String festivalSyncSource;      // ex. "한국관광공사 API"

    private double avgReviewScore;
    private long todayReviews;
    private long weekReviews;
    private long pendingReviewReports;      // 신고 처리 대기 리뷰 수

    private long todayPageViews;            // 오늘 페이지뷰(전체)
    private int  onlineUsers;               // 현재 접속 중(세션 기반)
    private int  maxOnlineToday;            // 오늘 최대 동접
    private long totalVisits;               // 전체 방문 수

    // 회원 요약
    private long todaySignupsDelta;         // 어제 대비 증감
    private long weeklyAvgSignups;
    private long reportedReviewsThisWeek;
    private double weekAvgReviewScore;

    private List<MemberSummaryDto> recentMembers;

    // 축제 요약
    private long todayStartFestivals;
    private long todayEndFestivals;
    private long weekFestivals;

    private FavoriteFestivalDto topFavoriteFestival; // 제목 + 즐겨찾기 수
    private String topFestivalStates;                       // "경북, 서울, 부산" 이런 식 문자열

    private List<FestivalSummaryDto> recentFestivals;

    // 주간 가입/리뷰 추이
    private List<DailyStatsDto> weeklyStats;

    // 세션/트래픽 요약
    private int   activeSessions;
    private long  todayNewSessions;
    private int   sessionTimeoutMinutes;
    private long  todayUniqueVisitors;
    private long  avgResponseTimeMs;
}

