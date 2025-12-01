/*MyPageController*/

package com.example.ex02.member.controller;

import com.example.ex02.member.dto.MypageProfileDto;
import com.example.ex02.member.dto.MypageAccountDto;
import com.example.ex02.member.dto.MypageFavoriteDto;
import com.example.ex02.member.dto.MypageReviewDto;
import com.example.ex02.member.entity.MemberEntity;
import com.example.ex02.member.service.MemberService;
import com.example.ex02.member.service.MyPageService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {

    private final MyPageService myPageService;
    private final MemberService memberService;
    // ⭐ 공통 메서드: 세션에서 로그인된 user_no 가져오기
    private Long getLoginUserNo(HttpSession session) {
        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");

        if (loginUser == null) {
            return null; // 로그인 안 된 상태
        }

        return loginUser.getUserNo();
    }

    /* ------------------------------
       프로필 페이지
    ------------------------------ */
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {

        Long userNo = getLoginUserNo(session);
        if (userNo == null) return "redirect:/login";

        model.addAttribute("member",
                myPageService.getProfile(userNo));

        return "mypage/profile";
    }

    /* ------------------------------
       회원정보 수정 화면
    ------------------------------ */
    @GetMapping("/account")
    public String account(HttpSession session, Model model) {

        Long userNo = getLoginUserNo(session);
        if (userNo == null) return "redirect:/login";

        model.addAttribute("member",
                myPageService.getAccount(userNo));

        return "mypage/account";
    }

    /* ------------------------------
       회원정보 수정 처리
    ------------------------------ */
    @PostMapping("/account/update")
    public String updateAccount(HttpSession session,
                                @ModelAttribute MypageAccountDto dto) {

        Long userNo = getLoginUserNo(session);
        if (userNo == null) return "redirect:/login";

        myPageService.updateMember(userNo, dto);

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            return "redirect:/mypage/account?success=true";
        }
        return "redirect:/mypage/account?updated=true";
    }

    /* ------------------------------
       회원 탈퇴
    ------------------------------ */
    @PostMapping("/withdraw")
    public String withdraw(HttpSession session) {

        Long userNo = getLoginUserNo(session);
        if (userNo == null) return "redirect:/login";

        myPageService.deactivate(userNo);
        session.invalidate();

        return "redirect:/";
    }
    /* ------------------------------
       리뷰 수정 처리
    ------------------------------ */
    @PostMapping("/reviews/update")
    public String updateReview(HttpSession session,
                               @RequestParam("reviewNo") Long reviewNo,
                               @RequestParam("content") String content,
                               @RequestParam("rating") Double rating) {

        Long userNo = getLoginUserNo(session);
        if (userNo == null) return "redirect:/login";

        myPageService.updateReview(reviewNo, content, rating);

        return "redirect:/mypage/reviews";
    }


    /* ------------------------------
       즐겨찾기 목록
    ------------------------------ */
    @GetMapping("/favorites")
    public String favorites(HttpSession session, Model model) {

        Long userNo = getLoginUserNo(session);
        if (userNo == null) return "redirect:/login";

        model.addAttribute("favorites",
                myPageService.getFavoriteList(userNo));

        return "mypage/mypage-favorites";
    }

    /* 리뷰 목록 (페이징 적용) */
    @GetMapping("/reviews")
    public String reviews(HttpSession session,
                          @RequestParam(defaultValue = "0") int page,
                          Model model) {

        Long userNo = getLoginUserNo(session);
        if (userNo == null) return "redirect:/login";

        // 10개씩 페이징
        Page<MypageReviewDto> reviewPage =
                myPageService.getMyReviews(userNo, page, 10);

        model.addAttribute("reviews", reviewPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reviewPage.getTotalPages());

        return "mypage/reviews";
    }


    /* ------------------------------
       리뷰 수정
    ------------------------------ */
    @GetMapping("/reviews/edit/{reviewNo}")
    public String editReview(HttpSession session,
                             @PathVariable Long reviewNo,
                             Model model) {

        Long userNo = getLoginUserNo(session);
        if (userNo == null) return "redirect:/login";

        model.addAttribute("review",
                myPageService.getReview(reviewNo));

        return "mypage/review-edit";
    }

    /* ------------------------------
       리뷰 삭제
    ------------------------------ */
    @GetMapping("/reviews/delete/{reviewNo}")
    public String deleteReview(HttpSession session,
                               @PathVariable Long reviewNo) {

        Long userNo = getLoginUserNo(session);
        if (userNo == null) return "redirect:/login";

        myPageService.deleteReview(reviewNo);

        return "redirect:/mypage/reviews";
    }

    /* ------------------------------
       즐겨찾기 삭제
    ------------------------------ */
    @PostMapping("/favorites/delete/{favoriteNo}")
    public String deleteFavorite(HttpSession session,
                                 @PathVariable Long favoriteNo) {

        Long userNo = getLoginUserNo(session);
        if (userNo == null) return "redirect:/login";

        myPageService.deleteFavorite(favoriteNo);

        return "redirect:/mypage/favorites";
    }

}
