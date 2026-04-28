package bluepill.server.service;

import bluepill.server.domain.User;
import bluepill.server.domain.UserToken;
import bluepill.server.exception.BusinessException;
import bluepill.server.exception.ErrorCode;
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

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler  extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException{
        OAuth2User oAth2User = (OAuth2User) authentication.getPrincipal();

        //Provider 가져오기
        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        User.Provider provider = User.Provider.valueOf(registrationId.toUpperCase());
        String providerId = oAth2User.getAttribute("sub");

        //DB에서 user 조회
        User user = userRepository.findByProviderAndProviderId(provider, providerId).orElseThrow(()-> new BusinessException(ErrorCode.USER_NOT_FOUND));


        // JWT 발급
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);

        // Refresh Token DB 저장
        UserToken userToken = UserToken.createToken(user, refreshToken);
        userTokenRepository.save(userToken);

        response.addCookie(createRefreshTokenCookie(refreshToken));

        String redirectUrl = "http://localhost:3000/callback";
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);

    }

    private Cookie createRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 로컬 개발에서는 false
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 7);
        return cookie;
    }
}
