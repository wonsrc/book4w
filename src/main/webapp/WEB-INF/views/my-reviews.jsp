<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="/WEB-INF/views/include/header.jsp" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Reviews</title>
    <style>
        /* 전체 테마 색상과 글꼴 조정 */
        body {
            font-family: 'Helvetica Neue', Arial, sans-serif;
            background-color: #1a1a2e;
            color: #e3e3e3;
            margin: 0;
            padding: 0;
        }
        h1 {
            text-align: center;
            font-size: 2.8rem;
            color: #e94560;
            margin: 20px 0;
        }
        .card {
            background-color: #16213e;
            border-radius: 16px;
            box-shadow: 0 6px 12px rgba(0, 0, 0, 0.4);
            margin: 20px auto;
            padding: 24px;
            width: 90%;
            max-width: 700px;
            color: #ffffff;
            transition: transform 0.3s, box-shadow 0.3s;
            text-decoration: none;
            display: block;
        }
        .card:hover {
            transform: translateY(-8px);
            box-shadow: 0 12px 24px rgba(0, 0, 0, 0.5);
            background-color: #0f3460;
        }
        .card-header {
            font-size: 1.7rem;
            font-weight: bold;
            color: #e94560;
            margin-bottom: 12px;
        }
        .card-rating {
            font-size: 1.2rem;
            color: #ffcc00;
            margin-bottom: 12px;
        }
        .card-content {
            font-size: 1.1rem;
            color: #dcdde1;
            line-height: 1.6;
        }
        .no-reviews {
            text-align: center;
            margin-top: 40px;
            color: #888;
            font-size: 1.4rem;
        }
        .pagination {
            display: flex;
            justify-content: center;
            margin-top: 30px;
        }
        .pagination a, .pagination span {
            margin: 0 6px;
            padding: 12px 18px;
            text-decoration: none;
            color: #0f3460;
            border: 1px solid #e94560;
            border-radius: 50%;
            transition: background-color 0.3s, color 0.3s;
            font-weight: bold;
        }
        .pagination a:hover {
            background-color: #e94560;
            color: white;
        }
        .pagination .active {
            background-color: #e94560;
            color: white;
            border: none;
        }
    </style>
</head>
<body>
    <h1>내 리뷰 관리</h1>

    <c:set var="user" value="${sessionScope['login']}" />
    <c:if test="${empty user}">
        <p>로그인이 필요합니다.</p>
    </c:if>

    <c:if test="${not empty myReviews}">
        <c:if test="${not empty myReviews.content}">
            <div>
                <c:forEach var="review" items="${myReviews.content}">
                    <a href="/board/detail/${review.bookId}" class="card">
                        <div class="card-header">
                            ${review.bookName} - ${review.writer}
                        </div>
                        <div class="card-rating">
                            평점: ★ <fmt:formatNumber value="${review.rating}" maxFractionDigits="1" />
                        </div>
                        <div class="card-content">
                            ${review.content}
                        </div>
                    </a>
                </c:forEach>
            </div>

            <div class="pagination">
                <c:if test="${myReviews.hasPrevious()}">
                    <a href="?page=${myReviews.number - 1}" aria-label="Previous">&laquo; 이전</a>
                </c:if>
                <c:forEach var="i" begin="0" end="${myReviews.totalPages - 1}">
                    <c:choose>
                        <c:when test="${i == myReviews.number}">
                            <span class="active">${i + 1}</span> <!-- Active class applied here -->
                        </c:when>
                        <c:otherwise>
                            <a href="?page=${i}">${i + 1}</a>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
                <c:if test="${myReviews.hasNext()}">
                    <a href="?page=${myReviews.number + 1}" aria-label="Next">다음 &raquo;</a>
                </c:if>
            </div>
        </c:if>

        <c:if test="${empty myReviews.content}">
            <p class="no-reviews">리뷰 목록이 없습니다. 리뷰를 작성해보세요!</p>
        </c:if>
    </c:if>

    <c:if test="${empty myReviews}">
        <p class="no-reviews">리뷰 목록이 없습니다. 리뷰를 작성해보세요!</p>
    </c:if>
</body>
</html>
