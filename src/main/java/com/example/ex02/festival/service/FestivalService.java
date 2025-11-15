package com.example.ex02.festival.service;

import com.example.ex02.festival.dto.FestivalDetailDto;
import com.example.ex02.festival.dto.FestivalListDto;
import com.example.ex02.festival.entity.FestivalDetailEntity;
import com.example.ex02.festival.entity.FestivalEntity;
import com.example.ex02.festival.entity.FavoriteEntity;
import com.example.ex02.festival.repository.FestivalDetailRepository;
import com.example.ex02.festival.repository.FestivalRepository;
import com.example.ex02.festival.repository.FavoriteRepository;
import com.example.ex02.member.entity.MemberEntity;
import com.example.ex02.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FestivalService {

    private final FestivalRepository festivalRepository;
    private final FestivalDetailRepository festivalDetailRepository;
    private final FavoriteRepository favoriteRepository;
    private final MemberRepository memberRepository;
}
