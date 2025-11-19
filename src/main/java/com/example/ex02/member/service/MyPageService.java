/*MyPageService*/

package com.example.ex02.member.service;

import com.example.ex02.festival.entity.FavoriteEntity;
import com.example.ex02.festival.entity.ReviewEntity;
import com.example.ex02.festival.repository.FavoriteRepository;
import com.example.ex02.festival.repository.FestivalImageRepository;
import com.example.ex02.festival.repository.ReviewRepository;
import com.example.ex02.member.dto.MypageAccountDto;
import com.example.ex02.member.dto.MypageFavoriteDto;
import com.example.ex02.member.dto.MypageProfileDto;
import com.example.ex02.member.entity.MemberEntity;
import com.example.ex02.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final MemberRepository memberRepository;
    private final FavoriteRepository favoriteRepository;
    private final ReviewRepository reviewRepository;
    private final FestivalImageRepository festivalImageRepository;



    /* ----------------------------------------
       프로필 조회 (DTO 반환)
    ---------------------------------------- */
    public MypageProfileDto getProfile(Long userNo) {
        MemberEntity m = memberRepository.findById(userNo)
                .orElse(null);

        if (m == null) return null;

        return MypageProfileDto.builder()
                .userNo(m.getUserNo())
                .userId(m.getUserId())
                .name(m.getName())
                .email(m.getEmail())
                .phone(m.getPhone())
                .gender(m.getGender())
                .birthDate(m.getBirthDate())
                .joinDate(m.getJoinDate())
                .favoriteTag(m.getFavoriteTag())
                .build();
    }


    /* ----------------------------------------
       회원정보 수정 화면용 조회
    ---------------------------------------- */
    public MypageAccountDto getAccount(Long userNo) {
        MemberEntity m = memberRepository.findById(userNo)
                .orElse(null);

        if (m == null) return null;

        return MypageAccountDto.builder()
                .userNo(m.getUserNo())
                .userId(m.getUserId())
                .name(m.getName())
                .email(m.getEmail())
                .phone(m.getPhone())
                .favoriteTag(m.getFavoriteTag())
                .build();
    }


    /* ----------------------------------------
       즐겨찾기 조회
    ---------------------------------------- */
    public List<MypageFavoriteDto> getFavoriteList(Long userNo) {
        List<FavoriteEntity> list = favoriteRepository.findByMember_UserNo(userNo);

        return list.stream().map(f -> {

            // 이미지 1개 가져오기
            String imageUrl = festivalImageRepository
                    .findFirstByFestival_FestivalNo(f.getFestival().getFestivalNo())
                    .map(img -> img.getImageUrl())
                    .orElse("/img/default.jpg");

            return MypageFavoriteDto.builder()
                    .favoriteNo(f.getFavoriteNo())
                    .festivalNo(f.getFestival().getFestivalNo())
                    .title(f.getFestival().getTitle())
                    .state(f.getFestival().getState())
                    .playtime(f.getFestival().getPlaytime())
                    .imageUrl(imageUrl) // 이미지 URL
                    .favoriteDate(f.getFavoriteDate())
                    .build();

        }).toList();
    }


    /* ----------------------------------------
       리뷰 조회
    ---------------------------------------- */
    public List<ReviewEntity> getReviewList(Long userNo) {
        return reviewRepository.findByMember_UserNo(userNo);
    }


    /* ----------------------------------------
       회원정보 수정
    ---------------------------------------- */
    public void updateMember(Long userNo, MypageAccountDto updated) {

        MemberEntity m = memberRepository.findById(userNo)
                .orElseThrow();

        m.setName(updated.getName());
        m.setEmail(updated.getEmail());
        m.setPhone(updated.getPhone());
        // 선호 태그 업데이트 추가
        m.setFavoriteTag(updated.getFavoriteTag());

        // 비밀번호 변경 시
        if (updated.getPassword() != null && !updated.getPassword().isEmpty()) {
            m.setPassword(updated.getPassword());
        }

        //  gender null 방지 (제일 중요)
        if (m.getGender() == null || m.getGender().trim().isEmpty()) {
            m.setGender("M");  // 기본 성별
        }

        memberRepository.save(m);
    }



    /* ----------------------------------------
       회원 탈퇴 (isActive = 'N')
    ---------------------------------------- */
    @Transactional
    public void deactivate(Long userNo) {

        MemberEntity member = memberRepository.findById(userNo)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        member.setIsActive("N");
        member.setWithdrawDate(LocalDate.now());

        memberRepository.save(member);
    }


    /* ----------------------------------------
   리뷰 상세 조회 (수정용)
---------------------------------------- */
    public ReviewEntity getReview(Long reviewNo) {
        return reviewRepository.findById(reviewNo)
                .orElse(null);
    }

    /* ----------------------------------------
       리뷰 수정
    ---------------------------------------- */
    public void updateReview(Long reviewNo, String content, Double rating) {

        ReviewEntity review = reviewRepository.findById(reviewNo)
                .orElseThrow();

        review.setContent(content);
        review.setRating(rating);
        review.setUpdatedAt(LocalDateTime.now());

        reviewRepository.save(review);
    }

    /* ----------------------------------------
       리뷰 삭제
    ---------------------------------------- */
    public void deleteReview(Long reviewNo) {
        reviewRepository.deleteById(reviewNo);
    }
    /* 즐겨찾기 삭제 */
    public void deleteFavorite(Long favoriteNo) {
        favoriteRepository.deleteById(favoriteNo);
    }
    @Transactional
    public void withdrawMember(Long userNo) {
        MemberEntity member = memberRepository.findById(userNo)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));

        member.setIsActive("N");
        member.setWithdrawDate(LocalDate.now());
    }

}
