package com.web.billim.member.service;

import com.web.billim.exception.JwtException;
import com.web.billim.email.service.EmailSendService;
import com.web.billim.exception.NotFoundException;
import com.web.billim.exception.UnAuthorizedException;
import com.web.billim.exception.handler.ErrorCode;
import com.web.billim.coupon.repository.CouponRepository;
import com.web.billim.coupon.service.CouponService;
import com.web.billim.infra.ImageUploadService;
import com.web.billim.member.domain.Member;
import com.web.billim.member.dto.request.*;
import com.web.billim.member.dto.UpdatePasswordCommand;
import com.web.billim.member.dto.response.HeaderInfoResponse;
import com.web.billim.member.dto.response.MyPageInfoResponse;
import com.web.billim.member.dto.response.MemberInfoResponse;
import com.web.billim.member.repository.MemberRepository;
import com.web.billim.point.dto.AddPointCommand;
import com.web.billim.point.service.PointService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class MemberService {

	private final ImageUploadService imageUploadService;
	private final CouponRepository couponRepository;
	private final CouponService couponService;
	private final PointService pointService;
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailSendService emailSendService;

	public Map<String, String> validateHandling(BindingResult bindingResult) {
		Map<String, String> validatorResult = new HashMap<>();

		for (FieldError error : bindingResult.getFieldErrors()) {
			String validKeyName = String.format("valid_%s", error.getField());
			validatorResult.put(validKeyName, error.getDefaultMessage());
		}
		return validatorResult;
	}

	@Transactional
	public void signUp(MemberSignupRequest memberSignupRequest) {
		memberSignupRequest.PasswordChange(passwordEncoder);
		Member member = memberRepository.save(memberSignupRequest.toEntity());

		// 쿠폰 주기
		couponRepository.findByName("회원가입 쿠폰")
			.map(coupon -> couponService.issueCoupon(member, coupon))
			.orElseThrow();

		// 포인트 주기
		AddPointCommand command = new AddPointCommand(member, 1000, LocalDateTime.now().plusDays(365));
		pointService.addPoint(command);
	}

	public boolean checkDuplicateNickname(String nickname) {
		return memberRepository.existsByNickname(nickname);
	}

	// Domain Service
	public Member retrieve(long memberId) {
		return memberRepository.findById(memberId)
				.orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));
	}

	@Transactional
	public MemberInfoResponse retrieveUpdateInfoPage(long memberId) {
		return memberRepository.findById(memberId)
				.map(MemberInfoResponse::from)
				.orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));
	}

	@Transactional
	public MyPageInfoResponse retrieveMyPageInfo(long memberId) {
		return memberRepository.findById(memberId).map(member -> {
			long availableAmount = pointService.retrieveAvailablePoint(memberId);
			long availableCouponCount = couponService.retrieveAvailableCouponList(memberId).size();
			return MyPageInfoResponse.of(member, availableAmount, availableCouponCount);
		}).orElseThrow();
	}

	@Transactional
	public void updateInfo(long memberId, MemberInfoUpdateRequest req) {
		memberRepository.findById(memberId).ifPresent(member -> {
			if (!member.getNickname().equals(req.getNickname())
				&& memberRepository.existsByNickname(req.getNickname())) {
				throw new RuntimeException("중복된 닉네임 입니다.");
			}
			String imageUrl = null;
			if (!(req.getNewProfileImage().isEmpty())) {
				imageUploadService.delete(member.getProfileImageUrl());
				imageUrl = imageUploadService.upload(req.getNewProfileImage(), "profile");
			}

			member.updateInfo(imageUrl, req.getNickname(), req.getAddress());
			memberRepository.save(member);
		});
	}

	@Transactional
	public void findPassword(FindPasswordRequest req) {
		Member member = memberRepository.findByEmailAndName(req.getEmail(), req.getName())
				.orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));
		String tempPassword = emailSendService.sendTempPassword(req);
		String encodedPassword = passwordEncoder.encode(tempPassword);
		member.changePassword(encodedPassword);
		// Dirty Checking
//		memberRepository.save(member);
	}

	@Transactional
	public void updatePassword(UpdatePasswordCommand command) {

		Member member = memberRepository.findById(command.getMemberId())
			.orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

		if (!passwordEncoder.matches(command.getPassword(), member.getPassword())) {
			throw new UnAuthorizedException(ErrorCode.INVALID_EMAIL_PASSWORD);
		}
		// member.validatePassword(passwordEncoder, command.getPassword());
		String encodedPassword = passwordEncoder.encode(command.getNewPassword());
		member.changePassword(encodedPassword);
	}

	public Member findById(long memberId) {
		return memberRepository.findById(memberId)
				.orElseThrow(()-> new JwtException(ErrorCode.MEMBER_NOT_FOUND));
	}

	@Transactional
	public HeaderInfoResponse retrieveHeaderInfo(long memberId) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));
		return HeaderInfoResponse.of(member);
	}


}
