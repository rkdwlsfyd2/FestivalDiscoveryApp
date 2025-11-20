package com.example.ex02.festival.service;

import com.example.ex02.festival.entity.FavoriteEntity;
import com.example.ex02.festival.repository.FavoriteRepository;
import com.example.ex02.festival.repository.FestivalRepository;
import com.example.ex02.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final FestivalRepository festivalRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void toggleFavorite(Long memberNo, Long festivalNo) {
        Optional<FavoriteEntity> opt =
                favoriteRepository.findByMemberUserNoAndFestivalFestivalNo(memberNo, festivalNo);

        if (opt.isPresent()) {
            favoriteRepository.delete(opt.get()); // 즐겨찾기 해제
        } else {
            FavoriteEntity fav = new FavoriteEntity();
            fav.setMember(memberRepository.getReferenceById(memberNo));
            fav.setFestival(festivalRepository.getReferenceById(festivalNo));
            fav.setFavoriteDate(LocalDateTime.now());
            favoriteRepository.save(fav);         // 즐겨찾기 추가
        }
    }
}

