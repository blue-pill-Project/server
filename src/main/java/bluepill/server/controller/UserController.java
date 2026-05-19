package bluepill.server.controller;

import bluepill.server.annotation.CurrentUserId;
import bluepill.server.dto.common.ApiResponse;
import bluepill.server.dto.user.UserProfileResponse;
import bluepill.server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
                                                           @Parameter(hidden = true) @CurrentUserId Long loginUserId){
        UserProfileResponse response = userService.getProfile(publicId, loginUserId);

        return ApiResponse.success("프로필 조회 성공", response);
    }
}
