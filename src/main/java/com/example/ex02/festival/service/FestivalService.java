package com.example.ex02.festival.service;

import com.example.ex02.festival.entity.FestivalEntity;
import com.example.ex02.festival.repository.FavoriteRepository;
import com.example.ex02.festival.repository.FestivalDetailRepository;
import com.example.ex02.festival.repository.FestivalImageRepository;
import com.example.ex02.festival.repository.FestivalRepository;
import com.example.ex02.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FestivalService {

    private final FestivalRepository festivalRepository;
	private final FestivalDetailRepository festivalDetailRepository;
	private final FestivalImageRepository festivalImageRepository;
    private final FavoriteRepository favoriteRepository;
    private final MemberRepository memberRepository;

	public FestivalEntity findFestivalById(Long festivalNo) {
		return festivalRepository.findById(festivalNo).orElse(null);
	}
}
