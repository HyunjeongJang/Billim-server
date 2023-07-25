package com.web.billim.product.repository;

import com.web.billim.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, ProductQueryDslRepository {

    List<Product> findAllByProductIdIn(List<Long> mostProductLists);

//    Page<Product> findAllByMemberId(Pageable paging);

    @Query("SELECT p FROM Product p WHERE p.member.memberId = :memberId")
    List<Product> findByMemberId(@Param("memberId") long memberId);


//    Page<Product> findAllByOrderByCreatedAtDesc(Pageable paging);
//    @Query("SELECT p FROM Product p "
//     + "WHERE p.productName like %:keyword% OR p.detail like %:keyword% ORDER BY p.createdAt DESC")
//    Page<Product> findAllByKeyword(@Param("keyword") String keyword, Pageable pageable);

//    List<Product> findAllByProductIdIn(List<Long> mostProductLists);

//    @Query(value = "SELECT * FROM product ORDER BY product_id DESC", nativeQuery = true)
//    Page<Product> findAllOrderByCreatedAtDesc(Pageable paging);

//     @Query("SELECT p FROM Product p WHERE p.member.memberId = :memberId")
//     List<Product> findByMemberId(@Param("memberId") int memberId);

//    Page<Product> findByProductNameAndDetailContaining(String keyword, Pageable pageable);

}



