package com.web.billim.order.service;

import com.web.billim.exception.ForbiddenException;
import com.web.billim.exception.NotFoundException;
import com.web.billim.exception.OrderFailedException;
import com.web.billim.exception.handler.ErrorCode;
import com.web.billim.member.domain.Member;
import com.web.billim.member.repository.MemberRepository;
import com.web.billim.member.service.MemberDomainService;
import com.web.billim.order.domain.ProductOrder;
import com.web.billim.order.dto.OrderCommand;
import com.web.billim.order.dto.response.MyOrderHistory;
import com.web.billim.order.dto.response.MySalesDetailResponse;
import com.web.billim.order.dto.response.PaymentInfoResponse;
import com.web.billim.order.repository.OrderRepository;
import com.web.billim.order.type.ProductOrderStatus;
import com.web.billim.order.util.LocalDateHelper;
import com.web.billim.payment.dto.PaymentCommand;
import com.web.billim.payment.service.PaymentService;
import com.web.billim.product.domain.Product;
import com.web.billim.product.service.ProductDomainService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final MemberRepository memberRepository;
    private final PaymentService paymentService;
    private final OrderRepository orderRepository;
    private final ProductDomainService productDomainService;
    private final OrderDomainService orderDomainService;

    // 이미 예약된 날짜 조회
    public List<LocalDate> reservationDate(long productId) {
        Product product = productDomainService.find(productId);
        List<ProductOrder> orderList = orderRepository.findAllByProductAndEndAtAfter(product, LocalDate.now());

        return orderList.stream()
                .filter(order -> order.getStatus().equals(ProductOrderStatus.DONE) || order.getStatus().equals(ProductOrderStatus.IN_PROGRESS))
                .flatMap(order -> LocalDateHelper.changeDate(order.getStartAt(), order.getEndAt()).stream())
                .filter(date -> !date.isBefore(LocalDate.now()))
                .collect(Collectors.toList());
    }

    // 주문 및 결제
    // product_order, payment 테이블에 INSERT
    // 1. 그러면 연속으로 2번 요청이 들어오면 당연히 2개의 INSERT 가 발생한다. -> Lock 을 이용하거나 하는 방식으로 해소..
    // 2. 아니면 중복 주문 검증로직에 걸려서 에러가 발생할 수도 있다.
    //   FE -> 1번 (200)
    //   FE -> 2번 (500) -> /failure
    @Transactional
    public PaymentInfoResponse order(long memberId, OrderCommand orderCommand) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        // 1. 해당 사용자가 주문중인게 있는지 확인
        if (orderDomainService.checkAlreadyInProgressOrder(member)) {
            throw new OrderFailedException(ErrorCode.ORDER_DUPLICATED_REQUEST);
        }

        // 2. 다른 사용자가 해당 Product 의 해당 기간을 결제중인게 있는지 확인
        Product product = productDomainService.find(orderCommand.getProductId());
        if (orderDomainService.checkAlreadyInProgressOrderOfOtherUser(product, orderCommand.getPeriod())) {
            throw new OrderFailedException(ErrorCode.ORDER_DUPLICATED_PERIOD);
        }

        // 3. 자기자신의 상품을 주문하는 케이스 확인
        if (product.isOwned(memberId)) {
            throw new OrderFailedException(ErrorCode.ORDER_OWN_PRODUCT);
        }

        // 4. 주문정보 생성
        ProductOrder order = orderRepository.save(ProductOrder.generateNewOrder(member, product, orderCommand));

        // 5. 결제정보 생성
        PaymentCommand paymentCommand = new PaymentCommand(member, order, orderCommand.getCouponIssueId(), orderCommand.getUsedPoint());
        return paymentService.payment(paymentCommand);
    }

    // 주문 취소
    @Transactional
    public void cancel(long orderId) {
        ProductOrder order = orderRepository.findById(orderId).orElseThrow();

        // 1. 이미 기간이 지난 건은 아닌지
        if (order.getEndAt().isBefore(LocalDate.now())) {
            throw new RuntimeException("기간이 지난 주문이라 취소할 수 없습니다.");
        }
        // 2. 결제 완료된 건인지
        if (!order.getStatus().equals(ProductOrderStatus.DONE)) {
            throw new RuntimeException("주문이 완료되지 않아 취소할 수 없습니다.");
        }

        order.cancel(); // Dirty Checking
        paymentService.cancel(order);
    }

    public ProductOrder findByOrder(long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow();
    }

//    // 마이페이지 나의 구매 내역 목록 조회
//    @Transactional
//    public MyOrderListResponse findMyOrder(long memberId) {
//        List<ProductOrder> productOrders = orderRepository.findAllByMember_memberId_OrderByOrderIdDesc(memberId);
//        List<MyOrderHistory> myOrderHistories = productOrders.stream()
//                .map(MyOrderHistory::from)
//                .collect(Collectors.toList());
//        return new MyOrderListResponse(myOrderHistories);
//    }

    // 마이페이지 나의 판매 내역 상세 조회
    @Transactional
    public MySalesDetailResponse findMySalesDetail(long memberId, long productId) {
        Product product = productDomainService.find(productId);
        if (product.isOwned(memberId)) {
            List<ProductOrder> orderHistories = orderRepository.findAllByProduct(product);
            return MySalesDetailResponse.of(product, orderHistories);
        } else {
            throw new ForbiddenException(ErrorCode.MISMATCH_MEMBER);
        }
    }

    @Transactional
    public Page<MyOrderHistory> myOrderHistory(long memberId, PageRequest paging) {
        Page<ProductOrder> orderPage =  orderRepository.findAllByMember_memberId_OrderByOrderIdDesc(memberId, paging);
        return orderPage.map(MyOrderHistory::from);
    }

}
