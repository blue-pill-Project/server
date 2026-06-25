package bluepill.server.config;

import bluepill.server.jwt.JwtFilter;
import bluepill.server.service.OAuth2SuccessHandler;
import bluepill.server.service.OAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, OAuth2SuccessHandler oAuth2SuccessHandler, JwtFilter jwtFilter, OAuth2UserService oAuth2UserService) throws Exception {
        http
                //csrf 비활성화 (jwt사용시 x)
                .csrf(AbstractHttpConfigurer::disable)

                //cors설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                //세션도 비활성화
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                //경로별 권한 설정
                .authorizeHttpRequests(auth->auth
                        //swagger 허용
                        .requestMatchers(
                                "/docs",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/auth/reissue",
                                "/auth/logout",
                                "/dev/token/**"

                                ).permitAll()

                        //캐릭터 카드 조회
                        .requestMatchers(HttpMethod.GET, "/character-cards", "/character-cards/*").permitAll()

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
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
