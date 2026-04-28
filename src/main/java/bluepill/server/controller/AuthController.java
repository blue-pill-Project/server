package bluepill.server.controller;


import bluepill.server.dto.common.ApiResponse;
import bluepill.server.dto.user.TokenResponse;
import bluepill.server.service.AuthService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="Auth")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/reissue")
    public ApiResponse<TokenResponse> reissue(@Parameter(
            name = "refreshToken",
            description = "Refresh Token",
            in = ParameterIn.COOKIE,
            required = true
    )@CookieValue(value = "refreshToken") String refreshToken) {
        TokenResponse response = authService.reissue(refreshToken);
        return ApiResponse.success("토큰이 재발급 되었습니다", response);
    }
}
