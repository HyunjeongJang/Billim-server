package com.web.billim.exception.handler;

import lombok.Builder;
import lombok.Getter;

import org.springframework.http.ResponseEntity;

import com.web.billim.exception.AuthenticationBusinessException;

@Getter
@Builder
public class ErrorResponse {
	private String code;
	private String message;

	public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode) {
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ErrorResponse.builder()
				.code(errorCode.name())
				.message(errorCode.getMessage())
				.build());
	}

	public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode, String message) {
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ErrorResponse.builder()
				.code(errorCode.name())
				.message(message)
				.build());
	}

	public static ErrorResponse from(BusinessException ex) {
		return new ErrorResponse(ex.getErrorCode().name(), ex.getMessage());
	}

	public static ErrorResponse from(AuthenticationBusinessException ex) {
		return new ErrorResponse(ex.getErrorCode().name(), ex.getMessage());
	}

}

