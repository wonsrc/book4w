package com.book4w.book4w.service;

import com.book4w.book4w.dto.request.ReviewPostRequestDTO;
import com.book4w.book4w.dto.response.ReviewResponseDTO;
import com.book4w.book4w.entity.Book;
import com.book4w.book4w.entity.Member;
import com.book4w.book4w.entity.Review;
import com.book4w.book4w.repository.BookRepository;
import com.book4w.book4w.repository.MemberRepository;
import com.book4w.book4w.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;


//    public Page<ReviewResponseDTO> getReviewList(String bookId, Pageable page) {
//        return reviewRepository.findByBookId(bookId, page).map(ReviewResponseDTO::new);
//    }

    public Page<ReviewResponseDTO> getReviewList(String bookId, Pageable pageable) {
        return reviewRepository.findByBookIdOrderByCreatedDateDesc(bookId, pageable)
                .map(ReviewResponseDTO::new);
    }


    public Review register(String bookId, ReviewPostRequestDTO dto) {
        // 책 정보 조회
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("책을 찾을 수 없습니다."));

        // 회원 정보 조회
        String memberUuid = dto.getMemberUuid();
        Member member = memberRepository.findById(memberUuid)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

        Review review = Review.builder()
                .id(UUID.randomUUID().toString())
                .member(member)
                .book(book)
                .content(dto.getContent())
                .rating(dto.getRating())
                .build();

        reviewRepository.save(review);
        return review;
    }


    public void updateReview(String reviewId, String newContent) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));

        // 리뷰의 내용 업데이트
        review.setContent(newContent);

        // 업데이트된 리뷰 저장
        reviewRepository.save(review);
    }

    // 특정 리뷰를 ID로 조회하는 메서드 추가
    public Review findById(String reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));
    }


    public void deleteReview(String reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}

