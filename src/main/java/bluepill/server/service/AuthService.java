package bluepill.server.service;

import bluepill.server.domain.User;
import bluepill.server.domain.UserToken;
import bluepill.server.dto.user.TokenResponse;
import bluepill.server.exception.BusinessException;
import bluepill.server.exception.ErrorCode;
import bluepill.server.jwt.JwtProvider;
import bluepill.server.repository.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProvider jwtProvider;
    private final UserTokenRepository userTokenRepository;

    @Transactional(readOnly = true)
    public TokenResponse reissue(String refreshToken) {
        System.out.println("받은 refreshToken = " + refreshToken);
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        UserToken userToken = userTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> {
                    System.out.println("DB에서 refreshToken 못 찾음");
                    return new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
                });

        User user = userToken.getUser();

        String accessToken = "Bearer " + jwtProvider.generateAccessToken(user);

        return new TokenResponse(accessToken);
    }
}
