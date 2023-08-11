package com.web.billim.chat.controller;

import static com.web.billim.chat.config.ChatConfig.*;

import java.util.List;

import com.web.billim.chat.dto.response.ChatRoomProductInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.web.billim.chat.dto.response.ChatMessageResponse;
import com.web.billim.chat.dto.response.ChatRoomAndPreviewResponse;
import com.web.billim.chat.dto.response.ChatRoomResponse;
import com.web.billim.chat.dto.request.SendImageMessageRequest;
import com.web.billim.chat.dto.request.SendTextMessageRequest;
import com.web.billim.chat.service.ChatMessageService;
import com.web.billim.chat.service.ChatRoomService;

import lombok.RequiredArgsConstructor;

@Tag(name = "채팅", description = "ChatController")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

	private final SimpMessagingTemplate messagingTemplate;
	private final ChatRoomService chatRoomService;
	private final ChatMessageService chatMessageService;

	@Operation(summary = "구매자가 채팅방 생성", description = "구매자가 처음으로 채팅방을 생성하고, 채팅방이 있으면 그냥 입장한다.")
	@PostMapping("/room/product/{productId}")
	public ResponseEntity<ChatRoomResponse> joinChatRoomForBuyer(@PathVariable long productId, @AuthenticationPrincipal long memberId) {
		return ResponseEntity.ok(chatRoomService.joinChatRoom(memberId, productId));
	}

	@Operation(summary = "판매자가 채팅방 생성", description = "판매자가가 판매목록에서 채팅방 생성한다.")
	@PostMapping("/room/product/{productId}/{buyerId}")
	public ResponseEntity<ChatRoomResponse> joinChatRoomForSeller(@PathVariable long productId, @PathVariable long buyerId) {
		return ResponseEntity.ok(chatRoomService.joinChatRoom(buyerId, productId));
	}


	// TODO : unreadCount 계산이 잘 안됨
	@Operation(summary = "채팅방 목록 조회", description = "자신의 채팅방 목록 전체 조회한다.")
	@GetMapping("/rooms")
	public ResponseEntity<List<ChatRoomAndPreviewResponse>> retrieveAllMyChatRoom(@AuthenticationPrincipal long buyerId) {
		return ResponseEntity.ok(chatRoomService.retrieveAllJoined(buyerId));
	}

	@Operation(summary = "판매자의 productId 에 따른 채팅방 목록 조회", description = "해당 상품에 대한 채팅방 전체 목록을 가져온다.")
	@GetMapping("/api/chat")
	public ResponseEntity<List<ChatRoomAndPreviewResponse>> retrieveAllProductChatRoom(@PathVariable long productId) {
		return ResponseEntity.ok(chatRoomService.retrieveAllByProductId(productId));
	}

	// TODO : 나간 후 다시 재입장했을 때 나가기 전 메시지 가리기 필요
	@Operation(summary = "채팅방 들어갔을 때 채팅 내용 조회", description = "채팅방 들어갔을 때 전체 채팅 목록을 불러온다.")
	@GetMapping("/messages/{chatRoomId}")
	public ResponseEntity<List<ChatMessageResponse>> retrieveAllChatMessage(@AuthenticationPrincipal long memberId, @PathVariable long chatRoomId) {
		return ResponseEntity.ok(chatRoomService.retrieveAllChatMessage(memberId, chatRoomId));
	}

	// TODO : 상품 정보(상품명, 금액, 사진) 내려주기
	@GetMapping("/product-info/{chatRoomId}")
	public ResponseEntity<ChatRoomProductInfo> retrieveProductInfo(@PathVariable long chatRoomId) {
		return ResponseEntity.ok(chatRoomService.getChatRoomProductInfo(chatRoomId));
	}

	@Operation(summary = "채팅방 나가기", description = "채팅방을 나가면 상대방에게 채팅방 나갔다는 시스템 메세지가 전송된다.")
	@DeleteMapping("/room/{chatRoomId}")
	public void exitChatRoom(@AuthenticationPrincipal long memberId, @PathVariable long chatRoomId) {
		chatRoomService.exit(memberId, chatRoomId);
	}

	@Operation(summary = "채팅 text 전송", description = "텍스트 형식의 채팅을 보낸다.")
	@MessageMapping("/send/text")
	public void sendMessage(SendTextMessageRequest req) {
		System.out.println("req = " + req);
		ChatMessageResponse message = chatMessageService.sendText(req);
		messagingTemplate.convertAndSend(MESSAGE_BROKER_SUBSCRIBE_PREFIX + "/chat/" + req.getChatRoomId(), message);
	}

	@Operation(summary = "채팅 이미지 전송", description = "이미지 형식의 채팅을 보낸다.")
	@MessageMapping("/send/image")
	public void sendMessage(SendImageMessageRequest req) {
		ChatMessageResponse message = chatMessageService.sendImage(req);
		messagingTemplate.convertAndSend(MESSAGE_BROKER_SUBSCRIBE_PREFIX + "/chat/" + req.getChatRoomId(), message);
	}

}
