package com.web.billim.product.service;

import com.web.billim.member.domain.Member;
import com.web.billim.member.repository.MemberRepository;
import com.web.billim.product.domain.Product;
import com.web.billim.product.domain.ProductInterest;
import com.web.billim.product.dto.request.InterestRequest;
import com.web.billim.product.dto.response.MyInterestProduct;
import com.web.billim.product.dto.response.MyInterestProductList;
import com.web.billim.product.repository.ProductInterestRepository;
import com.web.billim.product.repository.ProductRepository;
import com.web.billim.review.domain.Review;
import com.web.billim.review.dto.response.ProductReviewListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductInterestService {

    private final ProductInterestRepository productInterestRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void saveOrDeleteInterest(long memberId, InterestRequest interestRequest) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow();
        Product product = productRepository.findById(interestRequest.getProductId())
                .orElseThrow();
        if(interestRequest.getInterest()){
            ProductInterest productInterest = ProductInterest.builder()
                    .product(product)
                    .member(member)
                    .build();
            productInterestRepository.save(productInterest);
        } else{
            productInterestRepository.deleteByMemberAndProduct(member,product);
        }
    }

    // 내 관심목록 조회
    @Transactional
    public Page<MyInterestProduct> myInterestProduct(long memberId, PageRequest paging) {
        Page<ProductInterest> interestsPage = productInterestRepository.findAllByMember_memberId(memberId, paging);
        return interestsPage.map(MyInterestProduct::of);
    }

    @Transactional
    public MyInterestProductList myInterestProductList(long memberId) {
        List<MyInterestProduct> myInterestProduct= productInterestRepository.findAllByMember_memberId(memberId)
                .stream()
                .map(MyInterestProduct::of)
                .collect(Collectors.toList());
        return new MyInterestProductList(myInterestProduct);
    }



}
