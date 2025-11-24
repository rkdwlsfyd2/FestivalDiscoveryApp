package com.example.ex02.admin.controller;

import com.example.ex02.admin.dto.MemberDetailDto;
import com.example.ex02.admin.service.AdminService;
import com.example.ex02.member.dto.MemberDto;
import com.example.ex02.member.entity.MemberEntity;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
                             @PageableDefault(size = 20) Pageable pageable,
                             Model model) {

        Page<MemberDto> members =
                adminService.getMemberPage(keyword, role, isActive, pageable);

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
//        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");
//        if (loginUser == null || !"admin".equals(loginUser.getRole())) {
//            return "redirect:/member/login";
//        }

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

    // 축제 관리
    @GetMapping("festivals")
    public String festivalList(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String role,
                             @RequestParam(required = false) String isActive,
                             @PageableDefault(size = 20) Pageable pageable,
                             Model model) {

        Page<MemberDto> members =
                adminService.getMemberPage(keyword, role, isActive, pageable);

        model.addAttribute("members", members);
        model.addAttribute("keyword", keyword);
        model.addAttribute("role", role);
        model.addAttribute("isActive", isActive);

        // 사이드바 active 상태
        model.addAttribute("activeMenu", "festivals");

        return "admin/festival-list";
    }

    // 리뷰 관리
    @GetMapping("/reviews")
    public String reviewList(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String role,
                             @RequestParam(required = false) String isActive,
                             @PageableDefault(size = 20) Pageable pageable,
                             Model model) {

        Page<MemberDto> members =
                adminService.getMemberPage(keyword, role, isActive, pageable);

        model.addAttribute("members", members);
        model.addAttribute("keyword", keyword);
        model.addAttribute("role", role);
        model.addAttribute("isActive", isActive);

        // 사이드바 active 상태
        model.addAttribute("activeMenu", "reviews");

        return "admin/review-list";
    }
}
