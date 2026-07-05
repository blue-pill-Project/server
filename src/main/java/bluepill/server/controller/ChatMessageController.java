package bluepill.server.controller;

import bluepill.server.annotation.CurrentUserId;
import bluepill.server.dto.chat.ChatMessageListResponse;
import bluepill.server.dto.chat.ChatMessageRequest;
import bluepill.server.dto.chat.ChatMessageResponse;
import bluepill.server.dto.common.ApiResponse;
import bluepill.server.service.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name="Chat")
@RestController
@RequestMapping("/api/log-rooms")
@RequiredArgsConstructor
public class ChatMessageController {
    private final ChatMessageService chatMessageService;

    @Operation(summary = "채팅 전송")
    @PostMapping("/{publicId}/chats")
    public ApiResponse<ChatMessageResponse> send(
            @PathVariable UUID publicId,
            @RequestBody ChatMessageRequest request,
            @CurrentUserId Long userId
    ){
        ChatMessageResponse response = chatMessageService.send(publicId, request, userId);

        return ApiResponse.success("채팅 전송 성공", response);
    }

    @Operation(summary = "채팅방 메시지 목록 조회")
    @GetMapping("/{publicId}/chats")
    public ApiResponse<ChatMessageListResponse> getMessages(
            @PathVariable UUID publicId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size,
            @CurrentUserId Long userId
    ){
        ChatMessageListResponse response = chatMessageService.getMessages(publicId, cursor, size, userId);

        return ApiResponse.success("채팅방 메시지 목록 조회 성공", response);
    }
}
