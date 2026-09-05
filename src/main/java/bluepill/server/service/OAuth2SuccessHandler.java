package bluepill.server.service;

import bluepill.server.domain.User;
import bluepill.server.domain.UserToken;
import bluepill.server.exception.BusinessException;
import bluepill.server.exception.ErrorCode;
import bluepill.server.jwt.JwtConfig;
import bluepill.server.jwt.JwtProvider;
import bluepill.server.repository.user.UserRepository;
import bluepill.server.repository.user.UserTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler  extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final JwtConfig jwtConfig;
    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;

    @Value("${app.frontend.oauth-callback-url}")
    private String oauthCallbackUrl;

    @Value("${app.cookie.secure}")
    private boolean secure;

    @Value("${app.cookie.same-site}")
    private String sameSite;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException{
        OAuth2User oAth2User = (OAuth2User) authentication.getPrincipal();

        //Provider 가져오기
        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        User.Provider provider = User.Provider.valueOf(registrationId.toUpperCase());
        String providerId = getProviderId(provider, oAth2User);

        //DB에서 user 조회
        User user = userRepository.findByProviderAndProviderIdAndIsDeletedFalse(provider, providerId).orElseThrow(()-> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // JWT 발급
        String refreshToken = jwtProvider.generateRefreshToken(user);

        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtConfig.getRefreshTokenExpiration());

        // refreshToken생성, 만료 후 로그인일 경우 token값+만료일 update
        UserToken userToken  = userTokenRepository.findByUser(user)
                .map(token -> {
                    token.updateRefreshToken(refreshToken, expiresAt);
                    return token;
                })
                .orElseGet(() -> UserToken.createToken(user, refreshToken, expiresAt));
        userTokenRepository.save(userToken);

        response.addHeader(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(refreshToken).toString());

        getRedirectStrategy().sendRedirect(request, response, oauthCallbackUrl);
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(jwtConfig.getRefreshTokenExpiration())
                .sameSite(sameSite)
                .build();
    }

    private String getProviderId(User.Provider provider, OAuth2User oAuth2User) {
        return switch(provider){
            case GOOGLE -> oAuth2User.getAttribute("sub");
            case DISCORD -> oAuth2User.getAttribute("id");
        };
    }
}
