package com.example.ex02.admin.service;

import com.example.ex02.admin.dto.*;
import com.example.ex02.festival.dto.FestivalUpdateDto;
import com.example.ex02.festival.entity.FestivalDetailEntity;
import com.example.ex02.festival.entity.FestivalEntity;
import com.example.ex02.festival.entity.ReviewEntity;
import com.example.ex02.festival.repository.*;
import com.example.ex02.member.dto.MemberDto;
import com.example.ex02.member.entity.MemberEntity;
import com.example.ex02.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final MemberRepository memberRepository;
    private final FestivalRepository festivalRepository;
    private final ReviewRepository reviewRepository;
    private final FavoriteRepository favoriteRepository;
    private final FestivalDetailRepository festivalDetailRepository;
    private final FestivalTagRepository tagRepository;

    public AdminDashboardDto getDashboard() {
        // 이번주: 월요일 ~ 오늘
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);

        // 이번 달
        LocalDate thisMonthStart   = today.withDayOfMonth(1);
        LocalDate nextMonthStart = thisMonthStart.plusMonths(1);

        // 지난 달
        LocalDate lastMonthStart = thisMonthStart.minusMonths(1);
        LocalDate lastMonthEnd = thisMonthStart.minusDays(1);

        long totalMembers = memberRepository.count();
        long todayJoined = memberRepository.countByJoinDate(today);
        long yesterdayJoined = memberRepository.countByJoinDate(yesterday);
        long weekJoined = memberRepository.countByJoinDateBetween(weekStart, today);
        long totalFestivals = festivalRepository.count();
        long totalReviews = reviewRepository.count();

        long todaySignupsDelta = todayJoined - yesterdayJoined;

        // 이번 달 가입자 수
        long thisMonthJoined =
                memberRepository.countByJoinDateBetween(thisMonthStart, nextMonthStart.minusDays(1));

        // 지난 달 가입자 수
        long lastMonthJoined =
                memberRepository.countByJoinDateBetween(lastMonthStart, lastMonthEnd);

        // 성장률 계산: (이번달 - 지난달) / 지난달 * 100
        Double memberGrowthRate;

        if (lastMonthJoined == 0) {
            // 지난달 0명인 경우 나눗셈 방지용 처리
            if (thisMonthJoined == 0) {
                memberGrowthRate = 0.0;       // 0 → 0 : 0%
            } else {
                memberGrowthRate = 100.0;     // 0 → N : 100%
            }
        } else {
            memberGrowthRate =
                    ((double) (thisMonthJoined - lastMonthJoined) / lastMonthJoined) * 100.0;
        }

        memberGrowthRate = Math.round(memberGrowthRate * 10) / 10.0;

        // 최근 회원 5명
        List<MemberSummaryDto> recentMembers = memberRepository
                .findTop5ByOrderByJoinDateDesc()
                .stream()
                .map(MemberSummaryDto::from)
                .toList();

        // 최근 축제 5개
        List<FestivalSummaryDto> recentFestivals = festivalRepository
                .findTop5ByOrderByEventStartDateDesc()
                .stream()
                .map(FestivalSummaryDto::from)
                .toList();

        // ===== 축제 통계 =====
        LocalDateTime now = LocalDateTime.now();

        long ongoingFestivals  = festivalRepository.countOngoing(now);

        LocalDate firstDay     = LocalDate.now().withDayOfMonth(1);
        LocalDate firstNextMon = firstDay.plusMonths(1);
        LocalDateTime monthStart = firstDay.atStartOfDay();
        LocalDateTime nextMonthStart_ldt = firstNextMon.atStartOfDay();

        // 오늘 00:00 ~ 내일 00:00
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();

        // 이번 주 (월요일 ~ 다음 주 월요일)
        LocalDate weekStartDate = today.with(DayOfWeek.MONDAY);
        LocalDate weekEndDate = weekStartDate.plusWeeks(1); // 다음 주 월요일

//        LocalDateTime weekStart = weekStartDate.atStartOfDay();
        LocalDateTime weekEnd   = weekEndDate.atStartOfDay();
        long thisMonthFestivals =
                festivalRepository.countThisMonth(monthStart, nextMonthStart_ldt);

        long todayStartFestivals =
                festivalRepository.countFestivalStartingToday(startOfToday, startOfTomorrow);

        long todayEndFestivals =
                festivalRepository.countFestivalEndingToday(startOfToday, startOfTomorrow);

        // 이번 주 전체 기간 내에 "겹치는" 축제 개수
        long weekFestivals =
                festivalRepository.countFestivalsThisWeek(weekStart.atStartOfDay(), weekEnd);

        // ===== 리뷰 통계 =====
        Double avg = reviewRepository.findAverageRating();
        double averageRating = (avg != null) ? avg : 0.0;

        LocalDateTime startOfWeek = weekStartDate.atStartOfDay();   // 이번주 시작
        LocalDateTime endOfWeek   = weekEndDate.atStartOfDay();     // 이번주 끝

        long todayReviewCount =
                reviewRepository.countByCreatedAtBetween(startOfWeek, endOfWeek);

        Double avgWeekScore =
                reviewRepository.findAverageScoreBetween(startOfWeek, endOfWeek);

        // 소수점 한 자리까지 반올림
        if (avgWeekScore == null) {
            avgWeekScore = 0.0;
        } else {
            avgWeekScore = Math.round(avgWeekScore * 10) / 10.0;
        }

        // 신고 리뷰 수
//        long reportedReviewCount = reviewRepository.countByReportedTrue();


        // 전체 즐겨찾기 수
        long totalFavorites = favoriteRepository.count();

        // 즐겨찾기 TOP 조회
        FavoriteFestivalDto topFavoriteFestival;

        List<FavoriteRepository.TopFavoriteFestivalProjection> topList =
                favoriteRepository.findTopFavoriteFestival(PageRequest.of(0, 5));

        if (!topList.isEmpty()) {
            var p = topList.get(0);
            topFavoriteFestival = FavoriteFestivalDto.builder()
                    .festivalNo(p.getFestivalNo())
                    .title(p.getTitle())
                    .favoriteCount(p.getFavoriteCount())
                    .build();
        }else {
            topFavoriteFestival = FavoriteFestivalDto.builder()
                    .festivalNo(0L)
                    .title("데이터 없음")
                    .addr("")
                    .favoriteCount(0L)
                    .build();
        }

        return AdminDashboardDto.builder()
                .totalMembers(totalMembers)
                .todayJoined(todayJoined)
                .weekJoined(weekJoined)
                .memberGrowthRate(memberGrowthRate)
                .ongoingFestivals(ongoingFestivals)
                .thisMonthFestivals(thisMonthFestivals)
                .totalFestivals(totalFestivals)
                .totalReviews(totalReviews)
                .averageRating(averageRating)
                .todayReviewCount(todayReviewCount)
                .totalFavorites(totalFavorites)
                .todaySignupsDelta(todaySignupsDelta)
                .avgWeekScore(avgWeekScore)
                .recentMembers(recentMembers)
                .recentFestivals(recentFestivals)
                .todayStartFestivals(todayStartFestivals)
                .todayEndFestivals(todayEndFestivals)
                .weekFestivals(weekFestivals)
                .topFavoriteFestival(topFavoriteFestival)
                .topList(topList)
                .build();
    }


//    // 회원 목록 조회 (검색/필터 포함)
//    public List<MemberEntity> getMemberList(String keyword, String role, String isActive) {
//        return memberRepository.searchMembers(keyword, role, isActive );
//    }

    // 회원 목록 dto로 변환 (formattedphone 포함)
    public Page<MemberDto> getMemberPage(String keyword,
                                         String role,
                                         String isActive,
                                         Pageable pageable) {

        Page<MemberEntity> page = memberRepository.searchMembers(keyword, role, isActive, pageable);

        return page.map(this::toMemberDto);
    }

    private MemberDto toMemberDto(MemberEntity m) {
        return MemberDto.builder()
                .userNo(m.getUserNo())
                .userId(m.getUserId())
                .name(m.getName())
                .email(m.getEmail())
                .phone(m.getPhone())
                .gender(m.getGender())
                .birthDate(m.getBirthDate())
                .joinDate(m.getJoinDate())
                .role(m.getRole())
                .isActive(m.getIsActive())
                .withdrawDate(m.getWithdrawDate())
                .favoriteTag(m.getFavoriteTag())
                .build();
    }


    // 단일 회원 조회 (상세 보기 필요하면 사용)
    public MemberEntity getMember(Long userNo) {
        return memberRepository.findById(userNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. userNo=" + userNo));
    }

    public UserStatsDto getUserStats() {

        // 1. 나이대
        List<Object[]> ageRows = memberRepository.countMembersByAgeGroup();
        List<String> ageLabels = new ArrayList<>();
        List<Long> ageCounts = new ArrayList<>();

        for (Object[] row : ageRows) {
            Integer ageGroup = ((Number) row[0]).intValue();
            ageLabels.add(ageGroup + "대");
            ageCounts.add(((Number) row[1]).longValue());
        }

        // 2. 성별
        List<Object[]> genderRows = memberRepository.countMembersByGender();
        List<String> genderLabels = new ArrayList<>();
        List<Long> genderCounts = new ArrayList<>();

        for (Object[] row : genderRows) {
            Object genderObj = row[0];
            String genderCode = (genderObj == null) ? "U" : String.valueOf(genderObj); // 'M', 'F'

            long cnt = ((Number) row[1]).longValue();

            String label;
            switch (genderCode) {
                case "M":
                    label = "남자";
                    break;
                case "F":
                    label = "여자";
                    break;
                default:
                    label = "기타";
            }

            genderLabels.add(label);
            genderCounts.add(cnt);
        }

        // 3. 선호태그
        List<Object[]> tagRows = tagRepository.findTopTagCounts(7);
        List<String> tagLabels = new ArrayList<>();
        List<Long> tagCounts = new ArrayList<>();

        for (Object[] row : tagRows) {
            tagLabels.add((String) row[0]);
            tagCounts.add(((Number) row[1]).longValue());
        }

        return UserStatsDto.builder()
                .ageLabels(ageLabels)
                .ageCounts(ageCounts)
                .genderLabels(genderLabels)
                .genderCounts(genderCounts)
                .tagLabels(tagLabels)
                .tagCounts(tagCounts)
                .build();
    }



    // 활성 상태 변경 (Y/N)
    @Transactional
    public void changeActiveStatus(Long userNo, String isActive) {
        MemberEntity member = getMember(userNo);
        member.setIsActive(isActive);

        // 비활성 처리 시 탈퇴일 기록 (선택)
        if ("N".equals(isActive) && member.getWithdrawDate() == null) {
            member.setWithdrawDate(LocalDate.now());
        }
    }

    // 역할 변경 (user <-> admin)
    @Transactional
    public void changeRole(Long userNo, String role) {
        MemberEntity member = getMember(userNo);
        member.setRole(role);
    }

    // 강제 탈퇴 처리 같은 추가 기능이 필요하다면
    @Transactional
    public void withdrawMember(Long userNo) {
        MemberEntity member = getMember(userNo);
        member.setIsActive("N");
        member.setWithdrawDate(LocalDate.now());
    }

    public MemberDetailDto getMemberDetail(Long userNo) {
        MemberEntity member = memberRepository.findById(userNo)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다. userNo=" + userNo));

        // 리뷰 목록
        List<ReviewSummaryDto> reviews = reviewRepository
                .findByMemberUserNoWithFestival(userNo)
                .stream()
                .map(r -> ReviewSummaryDto.builder()
                        .reviewId(r.getReviewNo())
                        .festivalNo(r.getFestival().getFestivalNo())
                        .festivalTitle(r.getFestival().getTitle())
                        .content(r.getContent())
                        .rating(r.getRating())
                        .createdAt(r.getCreatedAt())
                        .build())
                .toList();

        // 즐겨찾기 목록
        List<FavoriteFestivalDto> favorites = favoriteRepository
                .findByMemberUserNoWithFestival(userNo)
                .stream()
                .map(fav -> FavoriteFestivalDto.builder()
                        .festivalNo(fav.getFestival().getFestivalNo())
                        .title(fav.getFestival().getTitle())
                        .addr(fav.getFestival().getAddr())
                        .build())
                .toList();

        return MemberDetailDto.builder()
                .member(member)
                .reviews(reviews)
                .favorites(favorites)
                .build();
    }

    // 리뷰 삭제 (관리자용)
    @Transactional
    public void deleteReviewByAdmin(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    public Page<ReviewSummaryDto> getReviewPage(String keyword,
                                                String sort,
                                                Pageable pageable) {

        Pageable sortedPageable = pageable;

        if ("ratingAsc".equals(sort)) {
            sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by("rating").ascending());
        }
        else if ("ratingDesc".equals(sort)) {
            sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by("rating").descending());
        }

        Page<ReviewEntity> page = reviewRepository.searchReviews(keyword, sortedPageable);

        return page.map(r -> ReviewSummaryDto.builder()
                .reviewId(r.getReviewNo())
                .userId(r.getMember().getUserId())
                .festivalTitle(r.getFestival().getTitle())
                .content(r.getContent())
                .rating(r.getRating())
                .createdAt(r.getCreatedAt())
                .build());
    }

    @Transactional
    public void updateFestival(FestivalUpdateDto dto) {

        // 1) 메인 엔티티 조회
        FestivalEntity festival = festivalRepository.findById(dto.getFestivalNo())
                .orElseThrow(() -> new IllegalArgumentException("해당 축제를 찾을 수 없습니다."));

        festival.setTitle(dto.getTitle());

        if (dto.getEventStartDate() != null) {
            festival.setEventStartDate(dto.getEventStartDate().atStartOfDay());
        }
        if (dto.getEventEndDate() != null) {
            festival.setEventEndDate(dto.getEventEndDate().atStartOfDay());
        }

        festival.setAddr(dto.getAddr());

        // 2) 디테일 엔티티 조회
        FestivalDetailEntity detail = festivalDetailRepository.findByFestivalNo(dto.getFestivalNo());

        if (detail == null) {
            throw new IllegalArgumentException("해당 축제의 상세 정보가 없습니다.");
        }

        // 3) FestivalEntity 수정
        festival.setEventStartDate(dto.getEventStartDate().atStartOfDay());
        festival.setEventEndDate(dto.getEventEndDate().atStartOfDay());
        festival.setAddr(dto.getAddr());

        // 4) FestivalDetailEntity 수정
        detail.setFestivalFee(dto.getFestivalFee());
        detail.setHost(dto.getHost());
        detail.setHostTel(dto.getHostTel());
        detail.setHomepage(dto.getHomepage());
        detail.setInfotext1(dto.getInfotext1());
        detail.setInfotext2(dto.getInfotext2());

        // 5) JPA @Transactional → 자동 dirty checking
        // 별도의 save() 필요 없음
    }

    @Transactional
    public void deleteFestival(Long festivalNo){
        // 1) 즐겨찾기 삭제
        favoriteRepository.deleteByFestivalNo(festivalNo);

        // 2) 리뷰 삭제
        reviewRepository.deleteByFestivalFestivalNo(festivalNo);

        // 3) 축제 상세 삭제
        festivalDetailRepository.deleteByFestivalNo(festivalNo);

        // 4) 축제 삭제
        festivalRepository.deleteById(festivalNo);
    }

}
