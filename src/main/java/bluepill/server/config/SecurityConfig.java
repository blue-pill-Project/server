package bluepill.server.config;

import bluepill.server.jwt.JwtFilter;
import bluepill.server.service.OAuth2SuccessHandler;
import bluepill.server.service.OAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, OAuth2SuccessHandler oAuth2SuccessHandler, JwtFilter jwtFilter, OAuth2UserService oAuth2UserService) throws Exception {
        http
                //csrf 비활성화 (jwt사용시 x)
                .csrf(AbstractHttpConfigurer::disable)

                //세션도 비활성화
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                //경로별 권한 설정
                .authorizeHttpRequests(auth->auth
                        //swagger 허용
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/auth/reissue",
                                "/auth/logout",
                                "/dev/token/**"
                                ).permitAll()

                        //그 외에는 요청 인증 필요
                        .anyRequest().authenticated()
                )
                //oauth2 엔드포인트를 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                            .userService(oAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();


    }
}
