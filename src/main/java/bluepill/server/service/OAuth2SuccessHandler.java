package bluepill.server.service;

import bluepill.server.domain.User;
import bluepill.server.domain.UserToken;
import bluepill.server.exception.BusinessException;
import bluepill.server.exception.ErrorCode;
import bluepill.server.jwt.JwtConfig;
import bluepill.server.jwt.JwtProvider;
import bluepill.server.repository.UserRepository;
import bluepill.server.repository.UserTokenRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException{
        OAuth2User oAth2User = (OAuth2User) authentication.getPrincipal();

        //Provider 가져오기
        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        User.Provider provider = User.Provider.valueOf(registrationId.toUpperCase());
        String providerId = getProviderId(provider, oAth2User);

        //DB에서 user 조회
        User user = userRepository.findByProviderAndProviderId(provider, providerId).orElseThrow(()-> new BusinessException(ErrorCode.USER_NOT_FOUND));

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

        response.addCookie(createRefreshTokenCookie(refreshToken));

        String redirectUrl = "http://localhost:5173/auth/callback";
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private Cookie createRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 로컬 개발에서는 false
        cookie.setPath("/");
        cookie.setMaxAge((int)jwtConfig.getRefreshTokenExpiration());
        return cookie;
    }

    private String getProviderId(User.Provider provider, OAuth2User oAuth2User) {
        return switch(provider){
            case GOOGLE -> oAuth2User.getAttribute("sub");
            case DISCORD -> oAuth2User.getAttribute("id");
        };
    }
}
