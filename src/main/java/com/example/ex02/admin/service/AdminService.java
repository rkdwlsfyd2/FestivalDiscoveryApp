package com.example.ex02.admin.service;

import com.example.ex02.admin.dto.*;
import com.example.ex02.festival.entity.FestivalEntity;
import com.example.ex02.festival.repository.FavoriteRepository;
import com.example.ex02.festival.repository.FestivalRepository;
import com.example.ex02.festival.repository.ReviewRepository;
import com.example.ex02.member.entity.MemberEntity;
import com.example.ex02.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final MemberRepository memberRepository;
    private final FestivalRepository festivalRepository;
    private final ReviewRepository reviewRepository;
    private final FavoriteRepository favoriteRepository;

    public AdminDashboardDto getDashboard() {

        long totalMembers = memberRepository.count();
        long todayJoined = memberRepository.countByJoinDate(LocalDate.now()); // 아래에 메서드 추가 필요
        long totalFestivals = festivalRepository.count();
        long totalReviews = reviewRepository.count();

        // 최근 가입한 회원 5명 조회 (Entity)
        List<MemberEntity> recentMemberEntities =
                memberRepository.findTop5ByOrderByJoinDateDesc();

        // 최근 축제 5개 조회 (Entity)
        List<FestivalEntity> recentFestivalEntities =
                festivalRepository.findTop5ByOrderByEventStartDateDesc();

        // 엔티티 -> 요약 DTO 변환
        List<MemberSummaryDto> recentMembers = recentMemberEntities.stream()
                .map(MemberSummaryDto::from)
                .toList();

        List<FestivalSummaryDto> recentFestivals = recentFestivalEntities.stream()
                .map(FestivalSummaryDto::from)
                .toList();

        // 대시보드 DTO 조립
        return AdminDashboardDto.builder()
                .totalMembers(totalMembers)
                .todayJoined(todayJoined)
                .totalFestivals(totalFestivals)
                .totalReviews(totalReviews)
                .recentMembers(recentMembers)
                .recentFestivals(recentFestivals)
                .build();
    }

    // 회원 목록 조회 (검색/필터 포함)
    public List<MemberEntity> getMemberList(String keyword, String role, String isActive) {
        return memberRepository.searchMembers(keyword, role, isActive);
    }

    // 단일 회원 조회 (상세 보기 필요하면 사용)
    public MemberEntity getMember(Long userNo) {
        return memberRepository.findById(userNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. userNo=" + userNo));
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
}
