package com.book4w.book4w.controller;

import com.book4w.book4w.dto.request.ReviewPostRequestDTO;
import com.book4w.book4w.dto.request.ReviewUpdateRequestDTO;
import com.book4w.book4w.dto.response.BookDetailResponseDTO;
import com.book4w.book4w.dto.response.DetailPageResponseDTO;
import com.book4w.book4w.dto.response.LoginUserResponseDTO;
import com.book4w.book4w.dto.response.ReviewResponseDTO;
import com.book4w.book4w.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.book4w.book4w.utils.LoginUtils.LOGIN_KEY;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
@Slf4j
public class BoardController {

    private final BoardService boardService;
    private final DetailService detailService;
    private final ReviewService reviewService;


    /*
    도서 목록 페이지
    @Param 모델, 요청 페이지 번호
     */
    @GetMapping("/list")
    public String list(Model model,
        @PageableDefault(size = 9) Pageable page,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String query) {

        // page 설정에 맞춰 북 목록을 Map에 저장하겠다.
        Page<BookDetailResponseDTO> bookPage;

        if (query != null && !query.trim().isEmpty()) {
            bookPage = getSortedBookPageForSearch(sort, page, query);
        } else {
            bookPage = getSortedBookPage(sort, page);
        }

        model.addAttribute("bList", bookPage.getContent());
        model.addAttribute("maker", bookPage);
        model.addAttribute("sort", sort);
        model.addAttribute("query", query);
        return "list";
    }

    private Page<BookDetailResponseDTO> getSortedBookPage(String sort, Pageable pageable) {
        if (sort == null) {
            return boardService.getBookList(pageable);
        }

        return switch (sort) {
            case "likeCount" -> boardService.getOrderLikeDesc(pageable);
            case "reviewCount" -> boardService.getOrderReviewDesc(pageable);
            case "rating" -> boardService.getOrderRatingDesc(pageable);
            default -> boardService.getBookList(pageable);
        };
    }

    private Page<BookDetailResponseDTO> getSortedBookPageForSearch(String sort, Pageable page,
        String query) {
        Page<BookDetailResponseDTO> books;

        if (sort == null) {
            books = boardService.searchByName(page, query);
        } else {
            books = switch (sort) {
                case "likeCount" -> boardService.searchByNameOrderByLikeDesc(page, query);
                case "reviewCount" -> boardService.searchBookByNameOrderByReviewDesc(page, query);
                case "rating" -> boardService.searchBookByNameOrderByRatingDesc(page, query);
                default -> boardService.searchByName(page, query);
            };
        }

        return books;
    }
    @GetMapping("/detail/{id}")
    public String detailPage(@PathVariable String id,
                             Model model,
                             HttpServletRequest request,
                             @PageableDefault(page = 0, size = 10) Pageable page) {
        log.info("Fetching detail for book id: {}", id);

        HttpSession session = request.getSession();
        LoginUserResponseDTO user = (LoginUserResponseDTO) session.getAttribute(LOGIN_KEY);
        String userId = user != null ? user.getUuid() : null;

        // 책 정보와 함께 좋아요 상태 및 좋아요 수 가져오기
        DetailPageResponseDTO bookDetail = detailService.getBookDetail(id, userId);
        boolean isLiked = bookDetail.isLiked(); // 사용자의 좋아요 상태
        int likeCount = bookDetail.getLikeCount(); // 좋아요 수
        Page<ReviewResponseDTO> reviewPage = reviewService.getReviewList(id, page);

        // 책 정보가 제대로 전달되는지 로그로 확인
        log.info("Book detail: {}", bookDetail);

        // 모델에 필요한 정보 추가
        model.addAttribute("book", bookDetail);
        model.addAttribute("isLiked", isLiked); // 좋아요 상태 추가
        model.addAttribute("likeCount", likeCount); // 좋아요 수 추가
        model.addAttribute("reviewList", reviewPage.getContent());
        model.addAttribute("page", reviewPage);

        return "detail"; // JSP 페이지로 이동
    }


    @PostMapping("/detail/{bookId}")
    @ResponseBody
    public Map<String, Object> addReview(@Validated @RequestBody ReviewPostRequestDTO dto,
                                         BindingResult result,
                                         @PathVariable String bookId,
                                         HttpSession session
                                         ) {
        Map<String, Object> response = new HashMap<>();

        // 입력값 검증
        if (result.hasErrors()) {
            response.put("success", false);
            response.put("message", "리뷰 입력값이 유효하지 않습니다.");
            return response;
        }

        // 로그인 여부 확인
        LoginUserResponseDTO loginUser = (LoginUserResponseDTO) session.getAttribute(LOGIN_KEY);
        if (loginUser == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return response;
        }

        // 사용자 정보 설정
        dto.setMemberUuid(loginUser.getUuid());

        try {
            // 리뷰 저장 처리
            reviewService.saveReview(bookId, dto);
            response.put("success", true);
            response.put("message", "리뷰가 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            log.error("리뷰 저장 중 오류 발생: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "리뷰 저장 중 오류가 발생했습니다.");
        }

        return response;
    }

    @GetMapping("/detail/{bookId}/reviews")
    @ResponseBody
    public Map<String, Object> getReviews(
            @PathVariable String bookId,
            @PageableDefault(size = 10) Pageable pageable) {

        // 특정 책의 페이징된 리뷰 목록 가져오기
        Page<ReviewResponseDTO> reviewPage = reviewService.getReviewList(bookId, pageable);

        // 응답 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("reviewList", reviewPage.getContent());
        response.put("page", Map.of(
                "number", reviewPage.getNumber(),
                "totalPages", reviewPage.getTotalPages(),
                "hasPrevious", reviewPage.hasPrevious(),
                "hasNext", reviewPage.hasNext()
        ));

        return response;
    }

    @PostMapping("/detail/{bookId}/review/{reviewId}/edit")
    @ResponseBody
    public Map<String, Object> editReview(@PathVariable String reviewId,
                                          @RequestBody ReviewUpdateRequestDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            reviewService.updateReview(reviewId, dto.getContent());
            response.put("success", true);
            response.put("message", "리뷰가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "리뷰 수정 중 오류가 발생했습니다.");
        }
        return response;
    }





    @PostMapping("/detail/{bookId}/toggle-like")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable String bookId, HttpServletRequest request) {
        log.info("/toggle-like: POST, {}", bookId);

        // 세션에서 로그인 사용자 정보 가져오기
        HttpSession session = request.getSession();
        LoginUserResponseDTO user = (LoginUserResponseDTO) session.getAttribute(LOGIN_KEY);

        // 사용자 UUID 출력
        if (user != null) {
            System.out.println("로그인 사용자 UUID: " + user.getUuid());
        }

        Map<String, Object> response = new HashMap<>();

        // 로그인 상태 확인
        if (user == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요하당께.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // 비로그인 시 UNAUTHORIZED 상태 코드 반환
        } else {
            log.info("로그인된 사용자 요청 - UUID: {}", user.getUuid());
        }
        System.out.println("null이 아니랑꼐요");

        // Issue 여기서 부터 시작.
        // 좋아요 토글 및 카운트 업데이트
        boolean isLiked = detailService.toggleLike(bookId, user.getUuid());

        int likeCount = detailService.getBookDetail(bookId, user.getUuid()).getLikeCount(); // 좋아요 수 업데이트
        response.put("success", true);
        response.put("isLiked", isLiked);
        response.put("likeCount", likeCount); // 좋아요 수를 응답에 포함

        return ResponseEntity.ok(response); // 성공 시 OK 상태 코드와 함께 반환
    }
}
