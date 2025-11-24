package com.example.ex02.admin.controller;

import com.example.ex02.admin.dto.AdminDashboardDto;
import com.example.ex02.admin.dto.MemberDetailDto;
import com.example.ex02.admin.dto.ReviewSummaryDto;
import com.example.ex02.admin.service.AdminService;
import com.example.ex02.festival.dto.FestivalUpdateDto;
import com.example.ex02.festival.entity.FestivalEntity;
import com.example.ex02.festival.entity.FestivalTagEntity;
import com.example.ex02.festival.service.FavoriteService;
import com.example.ex02.festival.service.FestivalService;
import com.example.ex02.member.dto.MemberDto;
import com.example.ex02.member.entity.MemberEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final FavoriteService favoriteService;
    private final FestivalService festivalService;

    // Jackson ObjectMapper (LocalDate 직렬화 포함)
    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper().registerModule(new JavaTimeModule());

    // 지역 리스트 (REGION_MAP의 key 값들)
    private static final List<String> REGION_LIST = Arrays.asList(
            "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종",
            "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"
    );
    
    @GetMapping
    public String adminHome(Model model) {
        model.addAttribute("activeMenu","dashboard");
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
        model.addAttribute("page", members.getNumber());
        model.addAttribute("totalPages", members.getTotalPages());

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
                               @RequestParam(required = false) Long userNo,
                               HttpSession session) {

        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");
        if (loginUser == null || !"admin".equals(loginUser.getRole())) {
            return "redirect:/member/login";
        }

        adminService.deleteReviewByAdmin(reviewId);

        // 회원 상세 페이지에서 삭제 → 그 페이지로 돌아감
        if (userNo != null) {
            return "redirect:/admin/members/" + userNo;
        }

        // 리뷰 관리 페이지에서 삭제 → 리뷰 목록으로
        return "redirect:/admin/reviews";
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
    public String festivalList(@RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "10") int size,
                             @RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(value = "ongoingOnly", defaultValue = "true") boolean ongoingOnly,
                             @RequestParam(required = false) String region,
                             @RequestParam(value = "tag", required = false) String tag,
                             HttpSession session,
                             Model model) {

        // 로그인 유저 + 즐겨찾기 목록
        MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");
        Set<Long> favoriteFestivalNos = Collections.emptySet();
        if (loginUser != null) {
            favoriteFestivalNos = favoriteService.getFavoriteFestivalNos(loginUser.getUserNo());
        }

        // 필터 조건에 맞는 축제 페이지 조회 (region 포함)
        Page<FestivalEntity> festivalList =
                findFestivalPage(page, size, keyword, ongoingOnly, region, tag);

        // 지도용 JSON (필요 필드만)
        String festivalsJson = buildFestivalsJson(festivalList);

        // 기본 모델
        model.addAttribute("ongoingOnly", ongoingOnly);
        model.addAttribute("region", region);
        model.addAttribute("tag", tag);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("festivalList", festivalList);
        model.addAttribute("totalCount", festivalList.getTotalElements());
        model.addAttribute("festivalsJson", festivalsJson);
        model.addAttribute("regionList", REGION_LIST);

        // 지도·태그·AI 공통 데이터
        addCommonFestivalMapAttributes(model, loginUser, favoriteFestivalNos,
                keyword, region, tag, ongoingOnly);

        // 사이드바 active 상태
        model.addAttribute("activeMenu", "festivals");

        return "admin/festival-list";
    }

    @PostMapping("/festivals/update")
    public String updateFestival(FestivalUpdateDto dto) {
        adminService.updateFestival(dto);
        return "redirect:/festivals/detail?festivalNo=" + dto.getFestivalNo();
    }

    @PostMapping("/festivals/delete")
    public String deleteFestival(@RequestParam Long festivalNo){
        adminService.deleteFestival(festivalNo);

        return "redirect:/admin/festivals";
    }


    /* ======================= private 헬퍼 메서드들 ======================= */

    /**
     * region / ongoingOnly / keyword / tag 를 모두 반영해서
     * 축제 페이지(Page<FestivalEntity>)를 조회하는 공통 메서드
     */
    private Page<FestivalEntity> findFestivalPage(int page,
                                                  int size,
                                                  String keyword,
                                                  boolean ongoingOnly,
                                                  String region,
                                                  String tag) {

        boolean hasRegion = (region != null && !region.isEmpty());

        if (hasRegion) {
            // 지역 필터가 있는 경우
            if (ongoingOnly) {
                return festivalService.getRegionOngoingFestivals(page, size, keyword, region, tag);
            } else {
                return festivalService.getRegionFestivals(page, size, keyword, region, tag);
            }
        } else {
            // 지역 필터가 없는 경우 (전체 지역)
            if (ongoingOnly) {
                return festivalService.getongoingFestivals(page, size, keyword, tag);
            } else {
                return festivalService.getonfutureFestivals(page, size, keyword, tag);
            }
        }
    }

    /**
     * 현재 페이지에 포함된 축제들만 가지고
     * 지도에서 쓸 간단한 JSON 형태로 변환
     */
    private String buildFestivalsJson(Page<FestivalEntity> festivalList) {
        try {
            List<Map<String, Object>> festivalDataList = festivalList.getContent().stream()
                    .map(festival -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("festivalNo", festival.getFestivalNo());
                        map.put("title", festival.getTitle());
                        map.put("addr", festival.getAddr());
                        map.put("mapx", festival.getMapx());
                        map.put("mapy", festival.getMapy());
                        map.put("eventStartDate", festival.getEventStartDate());
                        map.put("eventEndDate", festival.getEventEndDate());
                        return map;
                    })
                    .collect(Collectors.toList());

            return OBJECT_MAPPER.writeValueAsString(festivalDataList);
        } catch (Exception e) {
            // 로그 찍고 싶으면 여기서 log.error(...) 추가
            return "[]";
        }
    }

    /**
     * 지도/태그/AI 추천 관련 공통 모델 세팅
     * - 태그 Map
     * - 지도용 전체 축제 리스트
     * - AI 추천 축제 번호 Set
     * - 즐겨찾기/로그인 정보
     */
    private void addCommonFestivalMapAttributes(Model model,
                                                MemberEntity loginUser,
                                                Set<Long> favoriteFestivalNos,
                                                String keyword,
                                                String region,
                                                String tag,
                                                boolean ongoingOnly) {

        // 태그 Map
        Map<Long, List<FestivalTagEntity>> tagMap = festivalService.getTagMap();

        // 지도용 전체 축제 (현재 필터 조건 반영)
//        List<FestivalEntity> mapFestivals =
//                festivalService.getFestivalsForMap(keyword, region, tag, ongoingOnly);
//
//        // AI 추천 Top3
//        Set<Long> aiFestivalNos = Collections.emptySet();
//        if (loginUser != null) {
//            aiFestivalNos = aiScoreAppService.getTop3FestivalNosForFilteredFestivals(
//                    loginUser,
//                    mapFestivals
//            );
//        }

        model.addAttribute("tagMap", tagMap);
        model.addAttribute("favoriteFestivalNos", favoriteFestivalNos);
        model.addAttribute("loginUser", loginUser);
    }

    // 리뷰 관리
    @GetMapping("/reviews")
    public String reviewList(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String sort,
                             @PageableDefault(size = 10) Pageable pageable,
                             Model model) {

        Page<ReviewSummaryDto> reviews =
                adminService.getReviewPage(keyword, sort, pageable);

        model.addAttribute("reviews", reviews.getContent());
        model.addAttribute("page", reviews.getNumber());
        model.addAttribute("totalPages", reviews.getTotalPages());

        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);

        model.addAttribute("activeMenu", "reviews");

        return "admin/review-list";
    }

}
