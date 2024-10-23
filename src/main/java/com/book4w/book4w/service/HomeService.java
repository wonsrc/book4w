package com.book4w.book4w.service;

import com.book4w.book4w.dto.response.HomeRecommendedResponseDTO;
import com.book4w.book4w.entity.Book;
import com.book4w.book4w.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeService {
    private final BookRepository bookRepository;

    public HomeService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // 평점 순으로 3개의 책 추천
    public List<HomeRecommendedResponseDTO> recommendedListByRating() {
        List<Book> books = bookRepository.findTop3ByOrderByRatingDesc();  // 평점 순 상위 3개
        return books.stream()
                .map(book -> new HomeRecommendedResponseDTO(
//                        book.getId(),
                        book.getName(),
                        book.getWriter(),
                        book.getPub(),
                        book.getRating(),
                        book.getReviewCount(),
                        book.getLikeCount()
                )).toList();
    }

    // 리뷰 수 순으로 3개의 책 추천
    public List<HomeRecommendedResponseDTO> recommendedListByReviewCount() {
        List<Book> books = bookRepository.findTop3ByOrderByReviewCountDesc();  // 리뷰 수 상위 3개
        return books.stream()
                .map(book -> new HomeRecommendedResponseDTO(
//                        book.getId(),
                        book.getName(),
                        book.getWriter(),
                        book.getPub(),
                        book.getRating(),
                        book.getReviewCount(),
                        book.getLikeCount()
                )).toList();
    }

    // 좋아요 수 순으로 3개의 책 추천
    public List<HomeRecommendedResponseDTO> recommendedListByLikeCount() {
        List<Book> books = bookRepository.findTop3ByOrderByLikeCountDesc();  // 좋아요 수 상위 3개
        return books.stream()
                .map(book -> new HomeRecommendedResponseDTO(
//                        book.getId(),
                        book.getName(),
                        book.getWriter(),
                        book.getPub(),
                        book.getRating(),
                        book.getReviewCount(),
                        book.getLikeCount()
                )).toList();
    }
}