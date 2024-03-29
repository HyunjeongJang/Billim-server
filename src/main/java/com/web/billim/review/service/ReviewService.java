package com.web.billim.review.service;

import com.web.billim.order.domain.ProductOrder;
import com.web.billim.order.repository.OrderRepository;
import com.web.billim.order.service.OrderService;
import com.web.billim.point.service.PointDomainService;
import com.web.billim.point.dto.AddPointCommand;
import com.web.billim.point.service.PointService;
import com.web.billim.review.domain.Review;
import com.web.billim.review.dto.WrittenReviewList;
import com.web.billim.review.dto.request.ReviewWriteRequest;
import com.web.billim.review.dto.response.MyReviewListResponse;
import com.web.billim.review.dto.response.ProductReviewListResponse;
import com.web.billim.review.dto.WritableReviewList;
import com.web.billim.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final PointService pointService;
    private final PointDomainService pointDomainService;

    public double calculateStarRating(long productId) {
        return reviewRepository.findAllByProductId(productId).stream()
                .collect(Collectors.summarizingLong(Review::getStarRating))
                .getAverage();
    }

    @Transactional
    public void productReviewWrite(ReviewWriteRequest reviewWriteRequest) {
        ProductOrder productOrder = orderService.findByOrder(reviewWriteRequest.getOrderId());
        Review review = reviewRepository.save(ReviewWriteRequest.toEntity(reviewWriteRequest, productOrder));

        // 작성 시 적림금 부여
        long amount = pointDomainService.calculate(review);
        pointService.addPoint(new AddPointCommand(productOrder.getMember(), amount, Duration.ofDays(365)));
    }

    // 상품 디테일 리뷰 리스트
    public List<ProductReviewListResponse> reviewList(long productId) {
        return reviewRepository.findAllByProductId(productId)
                .stream()
                .map(ProductReviewListResponse::of)
                .collect(Collectors.toList());
    }

    // 상품 디테일 리뷰 리스트 - 리뷰 호출만 하는 api
    public Page<ProductReviewListResponse> productReviewList(long productId, PageRequest paging) {
        Page<Review> reviewPage = reviewRepository.findAllByProductId(productId, paging);
        return reviewPage.map(ProductReviewListResponse::of);
    }

    // 작성 가능한 리뷰 개수
    public long writableReviewCount(long memberId) {
        return orderRepository.findProductOrdersWritableReview(memberId).size();
    }

    // 작성 가능한 리뷰 리스트
    @Transactional
    public List<WritableReviewList> findMyWritableReview(long memberId) {
        return orderRepository.findProductOrdersWritableReview(memberId)
                .stream().map(WritableReviewList::of).collect(Collectors.toList());
    }

    // 나의 리뷰 리스트
    @Transactional
    public MyReviewListResponse myReviewList(long memberId) {
        List<WritableReviewList> writableReviewList = orderRepository.findProductOrdersWritableReview(memberId).stream()
                .map(WritableReviewList::of)
                .collect(Collectors.toList());
        List<WrittenReviewList> writtenReviewList = reviewRepository.findByProductOrder_Member_MemberId(memberId).stream()
                .map(WrittenReviewList::of)
                .collect(Collectors.toList());
        return new MyReviewListResponse(writableReviewList, writtenReviewList);
    }


}
