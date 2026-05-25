package bluepill.server.controller;

import bluepill.server.annotation.CurrentUserId;
import bluepill.server.dto.common.ApiResponse;
import bluepill.server.dto.user.UserProfileUpdateRequest;
import bluepill.server.dto.user.UserProfileResponse;
import bluepill.server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
@Tag(name="User")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "프로필 조회", description = "publicId로 사용자의 프로필 정보를 조회합니다. 로그인한 사용자가 본인의 프로필을 조회한 경우 isOwner가 true로 반환됩니다.")
    @GetMapping("/{publicId}")
    public ApiResponse<UserProfileResponse> getUserProfile(@Parameter(description = "조회할 사용자의 publicId", example="550e8400-e29b-41d4-a716-446655440000")
                                                           @PathVariable UUID publicId,
                                                           @Parameter(hidden = true) @CurrentUserId Long userId){
        UserProfileResponse response = userService.getProfile(publicId, userId);

        return ApiResponse.success("프로필 조회 성공", response);
    }

    @Operation(summary = "프로필 수정", description = """
            로그인한 사용자의 프로필(닉네임, 프로필 이미지)을 수정합니다.
            
            **닉네임 처리 정책**
            - 최초 로그인(닉네임 미설정) 상태에서 닉네임을 입력하지 않으면 랜덤 닉네임이 자동 부여됩니다.
            - 기존 닉네임이 있는 상태에서 닉네임을 비워서 요청하면 400 오류가 반환됩니다.
            - 이미 사용 중인 닉네임으로 수정 시 409 오류가 반환됩니다.
            
            **프로필 이미지 처리 정책**
            - 이미지 URL을 입력하지 않으면 기존 이미지가 유지됩니다.
            """)
    @PatchMapping("/me")
    public ApiResponse<UserProfileResponse> updateUserProfile(@Parameter(hidden = true) @CurrentUserId Long userId, @RequestBody UserProfileUpdateRequest request){
        UserProfileResponse response = userService.updateProfile(userId, request);

        return ApiResponse.success("프로필 수정 성공", response);
    }
}
