<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/views/include/header.jsp" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${book.name} - 상세 정보</title>
    <link rel="stylesheet" href="/css/style.css">
    <style>
        .book-detail-container {
            display: flex;
            justify-content: center;
            align-items: flex-start;
            padding: 20px;
        }

        .book-cover {
            width: 400px;
            height: 600px;
            margin-right: 20px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
        }

        .book-cover img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }

        .book-info {
            display: flex;
            flex-direction: column;
            justify-content: space-between;
        }

        .book-info h2 {
            margin-bottom: 20px;
        }

        .book-meta {
            margin-top: 20px;
        }

        .review-list {
            margin-top: 40px;
        }

        .review-form {
            margin-top: 20px;
        }

        .review-item {
            border-bottom: 1px solid #ccc;
            padding: 10px 0;
        }

        .edit-form {
            margin-top: 10px;
            display: none;
        }

        .pagination {
            margin-top: 20px;
            text-align: center;
        }

        .pagination a {
            margin: 0 5px;
            text-decoration: none;
            color: #333;
        }

        .pagination a.active {
            text-decoration: underline;
            font-weight: bold;
            color: #000;
        }

        .pagination a.disabled {
            color: #ccc;
            pointer-events: none;
        }
    </style>
</head>
<body>
<header>
    <h1>책 상세 정보</h1>
</header>

<div class="book-detail-container">
    <div class="book-cover">
        <c:choose>
            <c:when test="${not empty book.coverImage}">
                <img src="${book.coverImage}" alt="${book.name}의 표지"/>
            </c:when>
            <c:otherwise>
                <img src="https://via.placeholder.com/400x600" alt="기본 표지 이미지"/>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="book-info">
        <h2>${book.name}</h2>
        <p><strong>작가:</strong> ${book.writer}</p>
        <p><strong>출판사:</strong> ${book.pub}</p>
        <p><strong>출판년도:</strong> ${book.year}</p>
        <div class="book-meta">
            <p><strong>평점:</strong> ${book.rating} / 5.0</p>
            <p><strong>좋아요 수:</strong> ${book.likeCount}</p>
            <p><strong>리뷰 수:</strong> ${book.reviewCount}</p>
        </div>
    </div>
</div>

<div class="review-list">
    <h3>리뷰 목록</h3>
    <c:forEach var="review" items="${reviewList}">
    <div class="review-item" data-id="${review.id}">
        <p><strong>작성자:</strong> ${review.memberName} | <strong>내용:</strong> ${review.content} |
            <strong>평점:</strong> ${review.rating} / 5.0</p>
        <button onclick="showEditForm('${review.id}')">수정</button>
        <button onclick="deleteReview('${review.id}')">삭제</button>

        <!-- 리뷰 수정 폼 -->
        <div id="editForm-${review.id}" class="edit-form" style="display: none;">
            <textarea id="editContent-${review.id}">${review.content}</textarea>
            <button onclick="submitEdit('${review.id}')">저장</button>
            <button onclick="cancelEdit('${review.id}')">취소</button>
        </div>
    </div>
    </c:forEach>
</div>

<!-- 페이지 네비게이션 영역 -->
<div class="pagination">
    <!-- 이전 버튼 -->
    <c:if test="${page.hasPrevious()}">
        <a href="?page=${page.number - 1}">이전</a>
    </c:if>
    <c:if test="${!page.hasPrevious()}">
        <a class="disabled">이전</a>
    </c:if>

    <!-- 페이지 번호 링크 -->
    <c:forEach var="i" begin="0" end="${page.totalPages - 1}">
        <a href="?page=${i}" class="${page.number == i ? 'active' : ''}">
            ${i + 1}
        </a>
    </c:forEach>

    <!-- 다음 버튼 -->
    <c:if test="${page.hasNext()}">
        <a href="?page=${page.number + 1}">다음</a>
    </c:if>
    <c:if test="${!page.hasNext()}">
        <a class="disabled">다음</a>
    </c:if>
</div>

<!-- 리뷰 작성 폼 -->
<form id="reviewForm" class="review-form">
    <h4>리뷰 작성</h4>
    <textarea id="reviewContent" rows="3" cols="50" placeholder="리뷰 내용을 입력하세요"></textarea>
    <br>
    <label for="reviewRating">평점: </label>
    <select id="reviewRating">
        <c:forEach var="i" begin="1" end="5">
            <option value="${i}">${i}</option>
        </c:forEach>
    </select>
    <button type="submit">리뷰 작성</button>
</form>

<script>
    document.addEventListener("DOMContentLoaded", () => {
        const reviewForm = document.getElementById("reviewForm");

        if (!reviewForm) {
            console.error("리뷰 작성 폼을 찾을 수 없습니다.");
        }

        reviewForm.addEventListener("submit", (event) => {
            event.preventDefault(); // 폼 제출의 기본 동작(페이지 새로 고침)을 막습니다.
            console.log("리뷰 작성 폼 제출 이벤트 발생");

            const content = document.getElementById("reviewContent").value;
            const rating = document.getElementById("reviewRating").value;
            const reviewData = {
                content: content,
                rating: rating,
                memberUuid: '${user != null ? user.uuid : ""}'
            };

            console.log("보낼 리뷰 데이터:", reviewData);

            fetch(`/board/detail/${book.id}`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(reviewData)
            })
                .then(response => response.json())
                .then(data => {
                    console.log("서버 응답:", data);
                    if (data.success) {
                        addReviewToPage(data.content, data.rating, data.reviewId, data.nickname);
                        document.getElementById("reviewContent").value = '';
                        document.getElementById("reviewRating").value = '1';
                    } else {
                        console.error("Error:", data.message);
                        alert(data.message);
                    }
                })
                .catch(error => {
                    console.error("리뷰 작성 중 오류 발생:", error);
                    alert("리뷰 작성 중 오류가 발생했습니다. 다시 시도해주세요.");
                });
        });
    });

    function addReviewToPage(content, rating, reviewId, nickname) {
        const reviewList = document.querySelector(".review-list");
        const newReview = document.createElement("div");
        newReview.classList.add("review-item");
        newReview.dataset.id = reviewId;
        newReview.innerHTML = `
        <p><strong>작성자:</strong> ${nickname} | <strong>내용:</strong> ${content} |
        <strong>평점:</strong> ${rating} / 5.0</p>
        <button onclick="showEditForm('${reviewId}')">수정</button>
        <button onclick="deleteReview('${reviewId}')">삭제</button>
        <div id="editForm-${reviewId}" class="edit-form" style="display: none;">
            <textarea id="editContent-${reviewId}">${content}</textarea>
            <button onclick="submitEdit('${reviewId}')">저장</button>
            <button onclick="cancelEdit('${reviewId}')">취소</button>
        </div>
    `;
        reviewList.prepend(newReview);
    }


    function addReviewToPage(content, rating, reviewId, nickname) {
        const reviewList = document.querySelector(".review-list");
        const newReview = document.createElement("div");
        newReview.classList.add("review-item");
        newReview.dataset.id = reviewId;
        newReview.innerHTML = `
           <p><strong>작성자:</strong> ${nickname} | <strong>내용:</strong> ${content} |
           <strong>평점:</strong> ${rating} / 5.0</p>
           <button onclick="showEditForm('${reviewId}')">수정</button>
           <button onclick="deleteReview('${reviewId}')">삭제</button>
           <div id="editForm-${reviewId}" class="edit-form" style="display: none;">
               <textarea id="editContent-${reviewId}">${content}</textarea>
               <button onclick="submitEdit('${reviewId}')">저장</button>
               <button onclick="cancelEdit('${reviewId}')">취소</button>
           </div>
       `;
        reviewList.prepend(newReview);
    }

    function showEditForm(reviewId) {
        console.log('Function showEditForm called with reviewId:', reviewId);
        if (!reviewId) {
            console.error('Review ID is empty or undefined!');
            return;
        }

        console.log(`Looking for element with ID: editForm-${reviewId}`);
        const editForm = document.getElementById(`editForm-${reviewId}`);

        if (editForm) {
            console.log('Edit form found:', editForm);
            editForm.style.display = 'block';
        } else {
            console.error(`Edit form not found for review ID: ${reviewId}`);
        }
    }

    function cancelEdit(reviewId) {
        const editForm = document.getElementById(`editForm-${reviewId}`);
        if (editForm) {
            editForm.style
