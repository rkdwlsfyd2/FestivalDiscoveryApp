package com.example.ex02.admin.controller;

import com.example.ex02.admin.dto.MemberDetailDto;
import com.example.ex02.admin.service.AdminService;
import com.example.ex02.member.entity.MemberEntity;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    
    @GetMapping
    public String adminHome(Model model) {
        model.addAttribute("stats", adminService.getDashboard());
        return "admin/dashboard";
    }

    @GetMapping("/members")
    public String memberList(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String role,
                             @RequestParam(required = false) String isActive,
                             Model model) {

        List<MemberEntity> members = adminService.getMemberList(keyword, role, isActive);

        model.addAttribute("members", members);
        model.addAttribute("keyword", keyword);
        model.addAttribute("role", role);
        model.addAttribute("isActive", isActive);

        // 사이드바 active 상태
        model.addAttribute("activeMenu", "members");

        return "admin/member-list";
    }

    @GetMapping("/members/{userNo}")
    public String memberDetail(@PathVariable Long userNo,
                               HttpSession session,
                               Model model) {

        // 관리자 권한 체크 (인터셉터 쓴다면 생략 가능)
        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");
        if (loginUser == null || !"admin".equals(loginUser.getRole())) {
            return "redirect:/member/login";
        }

        MemberDetailDto detail = adminService.getMemberDetail(userNo);
        model.addAttribute("detail", detail);

        return "admin/member-detail";
    }

    // 리뷰 삭제 (관리자)
    @PostMapping("/reviews/{reviewId}/delete")
    public String deleteReview(@PathVariable Long reviewId,
                               @RequestParam Long userNo,
                               HttpSession session) {

        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");
        if (loginUser == null || !"admin".equals(loginUser.getRole())) {
            return "redirect:/member/login";
        }

        adminService.deleteReviewByAdmin(reviewId);
        return "redirect:/admin/members/" + userNo;
    }

    // 리뷰 수정 페이지로 이동 (기존 review-edit.html 재사용할 거면 거기로 리다이렉트)
    @GetMapping("/reviews/{reviewId}/edit")
    public String editReviewByAdmin(@PathVariable Long reviewId,
                                    @RequestParam Long userNo,
                                    Model model,
                                    HttpSession session) {

        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");
        if (loginUser == null || !"admin".equals(loginUser.getRole())) {
            return "redirect:/member/login";
        }

        // 기존 리뷰 상세 조회 로직 사용 (ReviewService 등)
        // model.addAttribute("review", reviewDto);
        // model.addAttribute("backToAdminMemberDetailUserNo", userNo);

        return "review/review-edit"; // 네가 쓰는 경로에 맞춰 수정
    }

    // 활성/비활성 변경
    @PostMapping("/members/{userNo}/active")
    public String changeActive(@PathVariable Long userNo,
                               @RequestParam String isActive,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) String role,
                               @RequestParam(required = false) String filterActive) {

        adminService.changeActiveStatus(userNo, isActive);

        // 다시 목록으로 redirect (검색 조건 유지)
        return "redirect:/admin/members"
                + buildQuery(keyword, role, filterActive);
    }

    // 역할 변경 (user/admin)
    @PostMapping("/members/{userNo}/role")
    public String changeRole(@PathVariable Long userNo,
                             @RequestParam String role,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String filterRole,
                             @RequestParam(required = false) String isActive) {

        adminService.changeRole(userNo, role);

        return "redirect:/admin/members"
                + buildQuery(keyword, filterRole, isActive);
    }

    // 강제 탈퇴 버튼용 (현재 그냥 isActive만 N 으로 바꿈)
    @PostMapping("/members/{userNo}/withdraw")
    public String withdraw(@PathVariable Long userNo,
                           @RequestParam(required = false) String keyword,
                           @RequestParam(required = false) String role,
                           @RequestParam(required = false) String isActive) {

        adminService.withdrawMember(userNo);

        return "redirect:/admin/members"
                + buildQuery(keyword, role, isActive);
    }

    private String buildQuery(String keyword, String role, String isActive) {
        StringBuilder sb = new StringBuilder("?");
        boolean first = true;

        if (keyword != null && !keyword.isEmpty()) {
            sb.append("keyword=").append(keyword);
            first = false;
        }
        if (role != null && !role.isEmpty()) {
            if (!first) sb.append("&");
            sb.append("role=").append(role);
            first = false;
        }
        if (isActive != null && !isActive.isEmpty()) {
            if (!first) sb.append("&");
            sb.append("isActive=").append(isActive);
        }

        // 아무 검색조건도 없으면 그냥 빈 문자열
        return sb.length() == 1 ? "" : sb.toString();
    }
}
