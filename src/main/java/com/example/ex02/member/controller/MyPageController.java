/*MyPageController*/

package com.example.ex02.member.controller;

import com.example.ex02.member.dto.MypageProfileDto;
import com.example.ex02.member.dto.MypageAccountDto;
import com.example.ex02.member.dto.MypageFavoriteDto;
import com.example.ex02.member.dto.MypageReviewDto;
import com.example.ex02.member.entity.MemberEntity;
import com.example.ex02.member.service.MyPageService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {

    private final MyPageService myPageService;

    // 로그인 임시 사용자 번호
    private final Long loginUserNo = 1L;

    /* ------------------------------
       프로필 페이지
    ------------------------------ */
    @GetMapping("/profile")
    public String profile(Model model) {

        MypageProfileDto profile = myPageService.getProfile(loginUserNo);

        model.addAttribute("member", profile);

        return "mypage/profile";
    }

    @GetMapping
    public String mypageHome() {
        return "mypage/index";
    }


    /* ------------------------------
       회원정보 수정 화면
    ------------------------------ */
    @GetMapping("/account")
    public String account(Model model) {

        MypageAccountDto account = myPageService.getAccount(loginUserNo);

        model.addAttribute("member", account);

        return "mypage/account";
    }

    /* ------------------------------
       회원정보 수정 (POST)
    ------------------------------ */
    @PostMapping("/account/update")
    public String updateAccount(@ModelAttribute MypageAccountDto dto) {

        // 기존 회원정보 + 비밀번호 변경 처리
        myPageService.updateMember(loginUserNo, dto);

        // 비밀번호가 입력된 경우 → 비밀번호 변경 성공 팝업을 띄우기 위한 파라미터 추가
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            return "redirect:/mypage/account?success=true";
        }

        // 그 외 수정은 updated=true (일반 정보 수정)
        return "redirect:/mypage/account?updated=true";
    }



    /* ------------------------------
       회원 탈퇴
    ------------------------------ */
    @PostMapping("/account/deactivate")
    public String deactivate() {

        myPageService.deactivate(loginUserNo);

        return "redirect:/logout";
    }

    /* ------------------------------
       즐겨찾기 목록
    ------------------------------ */
    @GetMapping("/favorites")
    public String favorites(Model model) {

        model.addAttribute("favorites",
                myPageService.getFavoriteList(loginUserNo));

        return "mypage/mypage-favorites";
    }

    /* ------------------------------
       리뷰 목록
    ------------------------------ */
    @GetMapping("/reviews")
    public String reviews(Model model) {

        model.addAttribute("reviews",
                myPageService.getReviewList(loginUserNo));

        return "mypage/reviews";
    }
    /* ------------------------------
   리뷰 수정 페이지 이동
------------------------------ */
    @GetMapping("/reviews/edit/{reviewNo}")
    public String editReview(@PathVariable Long reviewNo, Model model) {

        model.addAttribute("review",
                myPageService.getReview(reviewNo));

        return "mypage/review-edit";
    }

    /* ------------------------------
       리뷰 수정 처리
    ------------------------------ */
    @PostMapping("/reviews/update")
    public String updateReview(Long reviewNo,
                               Integer rating,
                               String content) {

        myPageService.updateReview(reviewNo, content, rating);

        return "redirect:/mypage/reviews";
    }

    /* ------------------------------
       리뷰 삭제
    ------------------------------ */
    @GetMapping("/reviews/delete/{reviewNo}")
    public String deleteReview(@PathVariable Long reviewNo) {

        myPageService.deleteReview(reviewNo);

        return "redirect:/mypage/reviews";
    }
    /* ------------------------------
   즐겨찾기 삭제
------------------------------ */
    @PostMapping("/favorites/delete/{favoriteNo}")
    public String deleteFavorite(@PathVariable Long favoriteNo) {

        myPageService.deleteFavorite(favoriteNo);

        return "redirect:/mypage/favorites";
    }

    @PostMapping("/withdraw")
    public String withdraw() {

        Long loginUserNo = 1L; // 실제 로그인 세션 값으로 바꿀 예정

        myPageService.withdrawMember(loginUserNo);

        return "redirect:/"; // 탈퇴 후 홈으로 이동
    }


}
