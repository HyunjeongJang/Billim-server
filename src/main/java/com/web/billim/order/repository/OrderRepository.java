package com.web.billim.order.repository;

import com.web.billim.member.domain.Member;
import com.web.billim.order.domain.ProductOrder;
import com.web.billim.order.type.ProductOrderStatus;
import com.web.billim.product.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<ProductOrder, Long> {

    List<ProductOrder> findAllByProductAndEndAtAfter(Product product, LocalDate now);
    Optional<ProductOrder> findByMemberAndStatus(Member member, ProductOrderStatus status);
    Optional<ProductOrder> findByProductAndStatus(Product product, ProductOrderStatus status);
    Page<ProductOrder> findAllByMember_memberId_OrderByOrderIdDesc(long memberId, Pageable pageable);
	List<ProductOrder> findAllByProduct(Product product);
	List<ProductOrder> findAllByEndAt(LocalDate datetime);

    @Query("SELECT po FROM ProductOrder po WHERE po.member.memberId = :memberId " +
            "AND po.status = 'DONE' AND po.endAt <= CURRENT_DATE " +
            "AND NOT EXISTS (SELECT r.productOrder FROM Review r INNER JOIN r.productOrder WHERE r.productOrder = po)")
    List<ProductOrder> findProductOrdersWritableReview(long memberId);

    List<ProductOrder> findByMember_memberId(Long memberId);
}

