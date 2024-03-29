package com.web.billim.order.controller;

import com.web.billim.order.dto.OrderCommand;
import com.web.billim.order.dto.response.*;
import com.web.billim.order.service.OrderService;
import com.web.billim.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "주문", description = "OrderController")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final ProductService productService;

    @Operation(summary = "상품 주문", description = "상품 결제버튼 클릭시 호출한다.")
    @PostMapping
    public ResponseEntity<PaymentInfoResponse> order(
            @AuthenticationPrincipal long memberId,
            @RequestBody OrderCommand command
    ) {
        PaymentInfoResponse resp = orderService.order(memberId, command);
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "상품 주문 취소", description = "상품 주문 및 결제를 취소한다.")
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancel(@PathVariable long orderId) {
        orderService.cancel(orderId);
        return ResponseEntity.ok().build();
    }

    // 페이징
    @Operation(summary = "마이페이지 상품 구매 목록 조회", description = "마이페이지에서 구매 목록을 조회한다.")
    @GetMapping("/my/purchase")
    public ResponseEntity<Page<MyOrderHistory>> myOrderList(
            @AuthenticationPrincipal long memberId,
            @RequestParam(required = false, defaultValue = "1") int page

    ) {
        PageRequest paging = PageRequest.of(page - 1, 4);
        Page<MyOrderHistory> myOrderHistoryPage = orderService.myOrderHistory(memberId, paging);
        return ResponseEntity.ok(myOrderHistoryPage);
    }

    @Operation(summary = "마이페이지 판매 목록 조회", description = "마이페이지에서 판매중인 상품 목록을 전체 조회한다.")
    @GetMapping("/my/sales")
    public ResponseEntity<Page<MySalesListResponse>> mySalesList(
            @AuthenticationPrincipal long memberId,
            @RequestParam(required = false, defaultValue = "1") int page
    ) {
        PageRequest paging = PageRequest.of(page - 1, 6);
        Page<MySalesListResponse> mySalesList = productService.findMySalesList(memberId, paging);
        return ResponseEntity.ok(mySalesList);
    }

    @Operation(summary = "마이페이지 판매 상품 상세정보", description = "판매중인 상품 클릭시 판매 주문 내역을 조회한다.")
    @GetMapping("/my/sales/{productId}")
    public ResponseEntity<MySalesDetailResponse> mySalesDetail(
            @AuthenticationPrincipal long memberId,
            @PathVariable("productId") long productId
    ) {
        return ResponseEntity.ok(orderService.findMySalesDetail(memberId, productId));
    }

}
