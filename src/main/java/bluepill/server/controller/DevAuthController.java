package bluepill.server.controller;

import bluepill.server.domain.User;
import bluepill.server.dto.common.ApiResponse;
import bluepill.server.dto.user.TokenResponse;
import bluepill.server.exception.BusinessException;
import bluepill.server.exception.ErrorCode;
import bluepill.server.jwt.JwtProvider;
import bluepill.server.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@Tag(name="DevAuth")
@Profile({"local"})
@RestController
@RequestMapping("/dev/token")
@RequiredArgsConstructor
public class DevAuthController {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Operation(
            summary = "테스트용 accessToken 발급",
            description = "개발/테스트 환경에서 userId로 accessToken을 발급합니다. 운영 환경에서는 비활성화됩니다."
    )
    @PostMapping
    public ApiResponse<TokenResponse> issueAccessToken(
            @Parameter(description = "토큰을 발급할 userId", example = "1")
            @RequestParam(defaultValue = "1") Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String accessToken = jwtProvider.generateAccessToken(user);

        return ApiResponse.success(
                "테스트용 accessToken이 발급되었습니다.",
                new TokenResponse(accessToken, user.getNickname() == null)
        );
    }
}
