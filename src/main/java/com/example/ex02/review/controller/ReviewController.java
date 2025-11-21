/*ReviewController*/
package com.example.ex02.review.controller;

import com.example.ex02.review.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReviewController {

	private final ReviewService reviewService;

	public ReviewController(ReviewService reviewService) {
		this.reviewService = reviewService;
	}

	@GetMapping("/designConfig")
	public String designConfigPage() {
		return "review/designConfig";
	}

	@GetMapping("/review")
	public String reviewPage() {
		return "review/review_integration";
	}

	// ★ 리뷰 등록
	@PostMapping("/festivals/review")
	public String createReview(@RequestParam("festivalNo") Long festivalNo,
							   @RequestParam("memberNo") Long memberNo,
							   @RequestParam("rating") Double rating,
							   @RequestParam("content") String content,
							   RedirectAttributes redirectAttributes) {

		try {
			reviewService.createReview(festivalNo, memberNo, rating, content);
			redirectAttributes.addFlashAttribute("msg", "리뷰가 등록되었습니다.");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
		}

		// 축제 상세 페이지 URL 패턴에 맞게만 수정
		return "redirect:/festivals/detail?festivalNo=" + festivalNo;
	}
}




