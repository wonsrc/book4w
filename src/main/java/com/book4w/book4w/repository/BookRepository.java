package com.book4w.book4w.repository;

import com.book4w.book4w.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {

    // 평점 순 상위 3개의 책을 조회하는 쿼리 메서드
    List<Book> findTop3ByOrderByRatingDesc();

    // 리뷰 수 순 상위 3개의 책을 조회하는 쿼리 메서드
    List<Book> findTop3ByOrderByReviewCountDesc();

    // 좋아요 수 순 상위 3개의 책을 조회하는 쿼리 메서드
    List<Book> findTop3ByOrderByLikeCountDesc();
}