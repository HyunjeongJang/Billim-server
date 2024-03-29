package com.web.billim.point.controller;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.web.billim.point.service.PointService;

@Tag(name = "적립금", description = "PointController")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/point")
public class PointController {

	private final PointService pointService;

	@Operation(summary = "사용 가능 적립금 금액 조회", description = "나의 사용 가능한 적립금 총 금액을 조회한다.")
	@GetMapping("/available")
	public ResponseEntity<Long> retrieveAvailablePoint(@RequestParam long memberId) {
		long availablePoint = pointService.retrieveAvailablePoint(memberId);
		return ResponseEntity.ok(availablePoint);
	}

	@Scheduled(cron = "0 0 0 * * *")
	public void savingPointScheduler() {
		log.info(String.format("[PointController] savingPointScheduler Action! (Time: %s)", LocalDateTime.now()));
		pointService.savingPointOrderHistory();
	}

	// 적립금 내역 조회

}
