package com.web.billim.review.controller;

import com.web.billim.review.dto.request.ReviewWriteRequest;
import com.web.billim.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "리뷰", description = "ReviewController")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/review")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 작성하기" , description = "리뷰를 작성한다. productId를 넘겨주세요.")
    @PostMapping("/write")
    public ResponseEntity<Void> productWrite(@RequestBody ReviewWriteRequest reviewWriteRequest) {
        reviewService.productReviewWrite(reviewWriteRequest);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "*리뷰 작성 가능한 상품 목록 불러오기", description = "마이페이지 헤더에서 후기작성 개수 클릭시 회원이 이용한 상품 중 리뷰 작성 가능한 상품 목록 불러온다.")
    @GetMapping("/writable")
    public ResponseEntity<Void> myWritableReview(@AuthenticationPrincipal long memberId) {
        reviewService.findMyWritableReview(memberId);
        return ResponseEntity.ok().build();
    }

    // 상품 리뷰


}
