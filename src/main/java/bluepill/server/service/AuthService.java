package bluepill.server.service;

import bluepill.server.domain.User;
import bluepill.server.domain.UserToken;
import bluepill.server.dto.user.TokenResponse;
import bluepill.server.exception.BusinessException;
import bluepill.server.exception.ErrorCode;
import bluepill.server.jwt.JwtProvider;
import bluepill.server.repository.UserRepository;
import bluepill.server.repository.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProvider jwtProvider;
    private final UserTokenRepository userTokenRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public TokenResponse reissue(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        UserToken userToken = userTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        User user = userToken.getUser();

        String accessToken = "Bearer " + jwtProvider.generateAccessToken(user);
        boolean isNewUser = user.getNickname() == null;

        return new TokenResponse(accessToken, isNewUser);
    }

    public void logout(String refreshToken) {
        if(refreshToken == null || refreshToken.isBlank()) return;

        userTokenRepository.findByRefreshToken(refreshToken).ifPresent(userTokenRepository :: delete);
    }
}
