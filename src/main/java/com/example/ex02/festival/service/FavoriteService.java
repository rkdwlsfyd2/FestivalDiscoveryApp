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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final FestivalRepository festivalRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public boolean toggleFavorite(Long memberNo, Long festivalNo) {
        Optional<FavoriteEntity> opt =
                favoriteRepository.findByMemberUserNoAndFestivalFestivalNo(memberNo, festivalNo);

        if (opt.isPresent()) {
            // 이미 즐겨찾기 되어 있으면 → 해제
            favoriteRepository.delete(opt.get());
            return false;   // removed
        } else {
            // 즐겨찾기 추가
            FavoriteEntity fav = new FavoriteEntity();
            fav.setMember(memberRepository.getReferenceById(memberNo));
            fav.setFestival(festivalRepository.getReferenceById(festivalNo));
            fav.setFavoriteDate(LocalDateTime.now());
            favoriteRepository.save(fav);
            return true;    // added
        }
    }



    // 해당 유저가 특정 축제를 즐겨찾기 했는지 여부
    public boolean isFavorite(Long userNo, Long festivalNo) {
        if (userNo == null || festivalNo == null) {
            return false;
        }
        return favoriteRepository.existsByMemberUserNoAndFestivalFestivalNo(userNo, festivalNo);
    }

    // 해당 유저의 모든 즐겨찾기 축제 번호 Set
    public Set<Long> getFavoriteFestivalNos(Long userNo) {
        return favoriteRepository.findByMember_UserNo(userNo).stream()
                .map(fav -> fav.getFestival().getFestivalNo())
                .collect(Collectors.toSet());
    }
}
