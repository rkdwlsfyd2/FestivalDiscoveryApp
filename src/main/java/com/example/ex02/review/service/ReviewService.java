package com.example.ex02.review.service;

import com.example.ex02.festival.entity.FestivalEntity;
import com.example.ex02.festival.entity.ReviewEntity;
import com.example.ex02.festival.repository.FestivalRepository;
import com.example.ex02.festival.repository.ReviewRepository;
import com.example.ex02.member.entity.MemberEntity;
import com.example.ex02.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final FestivalRepository festivalRepository;
	private final MemberRepository memberRepository;

	@Transactional
	public void createReview(Long festivalNo,
							 Long memberNo,
							 Double rating,
							 String content) {

		FestivalEntity festival = festivalRepository.findById(festivalNo)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 축제입니다."));

		MemberEntity member = memberRepository.findById(memberNo)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

		ReviewEntity review = ReviewEntity.builder()
				.festival(festival)
				.member(member)
				.rating(rating)
				.content(content)
				.build();

		reviewRepository.save(review);

	}

	// 해당 축제에 로그인 유저 리뷰가 이미 있는지 체크
	@Transactional(readOnly = true)
	public boolean hasUserReview(Long festivalNo, Long memberNo) {
		return reviewRepository
				.existsByFestival_FestivalNoAndMember_UserNo(festivalNo, memberNo);
	}

	// 축제 리뷰 페이징 조회 (최신순)
	@Transactional(readOnly = true)
	public Page<ReviewEntity> getFestivalReviews(Long festivalNo, int page, int size) {
		Pageable pageable = PageRequest.of(
				page,
				size,
				Sort.by(Sort.Direction.DESC, "createdAt")  // 최신 작성 순
		);
		return reviewRepository.findByFestival_FestivalNo(festivalNo, pageable);
	}
}
