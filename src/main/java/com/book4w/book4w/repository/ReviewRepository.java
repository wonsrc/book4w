package com.book4w.book4w.repository;

import com.book4w.book4w.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, String> {

}