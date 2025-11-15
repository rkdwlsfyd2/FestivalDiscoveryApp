package com.example.ex02.admin.service;

import com.example.ex02.admin.dto.AdminDashboardDto;
import com.example.ex02.festival.repository.FestivalRepository;
import com.example.ex02.festival.repository.ReviewRepository;
import com.example.ex02.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final MemberRepository memberRepository;
    private final FestivalRepository festivalRepository;
    private final ReviewRepository reviewRepository;

    public AdminDashboardDto getDashboard() {
        long memberCount = memberRepository.count();
        long festivalCount = festivalRepository.count();
        long reviewCount = reviewRepository.count();

        return AdminDashboardDto.builder()
                .memberCount(memberCount)
                .festivalCount(festivalCount)
                .reviewCount(reviewCount)
                .build();
    }
}
