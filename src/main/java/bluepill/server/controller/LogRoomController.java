package bluepill.server.controller;

import bluepill.server.annotation.CurrentUserId;
import bluepill.server.dto.common.ApiResponse;
import bluepill.server.dto.logroom.DayLogTimeSlot;
import bluepill.server.dto.logroom.LogCharacterCardResponse;
import bluepill.server.dto.logroom.LogPhotoUploadRequest;
import bluepill.server.dto.logroom.LogPhotoUploadResponse;
import bluepill.server.dto.logroom.LogRoomCreateRequest;
import bluepill.server.dto.logroom.LogRoomCreateResponse;
import bluepill.server.dto.logroom.LogRoomListResponse;
import bluepill.server.dto.post.PostShareRequest;
import bluepill.server.dto.post.PostShareResponse;
import bluepill.server.service.LogRoomService;
import bluepill.server.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/log-rooms")
@RequiredArgsConstructor
public class LogRoomController {

    private final LogRoomService logRoomService;
    private final PostService postService;

    @GetMapping
    public ResponseEntity<ApiResponse<LogRoomListResponse>> getMyLogRooms(
            @CurrentUserId Long userId,
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "10") int size) {

        LogRoomListResponse response = logRoomService.getMyLogRooms(userId, cursor, size);

        return ResponseEntity.ok(
                ApiResponse.success("로그방 목록 조회 성공", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LogRoomCreateResponse>> createLogRoom(
            @CurrentUserId Long userId,
            @RequestBody @Valid LogRoomCreateRequest request) {

        LogRoomCreateResponse response = logRoomService.createLogRoom(request, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("로그방 생성 성공", response));
    }

    @GetMapping("/{publicId}/logs")
    public ResponseEntity<ApiResponse<List<DayLogTimeSlot>>> getDayLog(
            @CurrentUserId Long userId,
            @PathVariable UUID publicId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<DayLogTimeSlot> response = logRoomService.getDayLog(publicId, date, userId);

        return ResponseEntity.ok(
                ApiResponse.success("하루 로그 조회 성공", response));
    }

    @GetMapping("/{publicId}/members/{memberPublicId}")
    public ResponseEntity<ApiResponse<LogCharacterCardResponse>> getLogCharacterCard(
            @CurrentUserId Long userId,
            @PathVariable UUID publicId,
            @PathVariable UUID memberPublicId) {

        LogCharacterCardResponse response = logRoomService.getLogCharacterCard(publicId, memberPublicId, userId);

        return ResponseEntity.ok(
                ApiResponse.success("로그 캐릭터 카드 조회 성공", response));
    }

    @PatchMapping("/{publicId}/members/{memberPublicId}")
    public ResponseEntity<ApiResponse<Void>> updateLogCharacterCard(
            @CurrentUserId Long userId,
            @PathVariable UUID publicId,
            @PathVariable UUID memberPublicId) {

        logRoomService.updateLogCharacterCard(publicId, memberPublicId, userId);

        return ResponseEntity.ok(
                ApiResponse.success("최신 버전으로 업데이트되었습니다."));
    }

    @PostMapping("/{publicId}/posts")
    public ResponseEntity<ApiResponse<PostShareResponse>> sharePost(
            @CurrentUserId Long userId,
            @PathVariable UUID publicId,
            @RequestBody @Valid PostShareRequest request) {

        PostShareResponse response = postService.sharePost(publicId, request, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("게시물이 공유되었습니다.", response));
    }

    @PostMapping("/{publicId}/photos")
    public ResponseEntity<ApiResponse<LogPhotoUploadResponse>> uploadPhoto(
            @CurrentUserId Long userId,
            @PathVariable UUID publicId,
            @RequestHeader(name = "X-Timezone", required = false) String timezone,
            @RequestBody @Valid LogPhotoUploadRequest request) {

        LogPhotoUploadResponse response = logRoomService.uploadPhoto(publicId, timezone, request, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("사진 업로드 성공", response));
    }
}
