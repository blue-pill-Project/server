package bluepill.server.jwt;

import bluepill.server.config.JwtConfig;
import bluepill.server.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final JwtConfig jwtConfig;


    //AccessToken 생성
    public String generateAccessToken(User user) {
        Instant now = Instant.now(); //현재시간(Utc)

        JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256).build();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(user.getUserId()))
                .claim("publicId", user.getPublicId().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(jwtConfig.getAccessTokenExpiration()))
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(headers,claims)).getTokenValue();
    }

    //RefreshToken 생성
    public String generateRefreshToken(User user){
        Instant now = Instant.now(); //현재시간(Utc)

        JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256).build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(user.getUserId()))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(jwtConfig.getRefreshTokenExpiration())) //7일
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(headers,claims)).getTokenValue();
    }

    //토큰 검증
    public boolean validateToken(String token) {
        try {
            jwtDecoder.decode(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //UserId 추출
    public Long getUserId(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        return Long.parseLong(jwt.getSubject());
    }
}
