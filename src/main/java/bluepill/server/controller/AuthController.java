package bluepill.server.controller;


import bluepill.server.dto.common.ApiResponse;
import bluepill.server.dto.user.TokenResponse;
import bluepill.server.service.AuthService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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

    @Value("${app.cookie.secure}")
    private boolean secure;

    @Value("${app.cookie.same-site}")
    private String sameSite;

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

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@CookieValue(value = "refreshToken") String refreshToken, HttpServletResponse response) {
        authService.logout(refreshToken);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(secure) //운영에서 true
                .path("/")
                .maxAge(0)
                .sameSite(sameSite)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ApiResponse.success("로그아웃 되었습니다.", null);
    }
}
