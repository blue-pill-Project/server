package bluepill.server.controller;

import bluepill.server.annotation.CurrentUserId;
import bluepill.server.dto.common.ApiResponse;
import bluepill.server.dto.logroom.LogRoomListResponse;
import bluepill.server.service.LogRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/log-rooms")
@RequiredArgsConstructor
public class LogRoomController {

    private final LogRoomService logRoomService;

    @GetMapping
    public ResponseEntity<ApiResponse<LogRoomListResponse>> getMyLogRooms(
            @CurrentUserId Long userId,
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "10") int size) {

        LogRoomListResponse response = logRoomService.getMyLogRooms(userId, cursor, size);

        return ResponseEntity.ok(
                ApiResponse.success("로그방 목록 조회 성공", response));
    }
}
