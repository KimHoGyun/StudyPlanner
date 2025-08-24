package com.studyapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (API 서버이므로)
                .csrf().disable()

                // CORS 설정 - 더 관대하게 설정
                .cors().configurationSource(corsConfigurationSource())
                .and()

                // 세션 사용하지 않음 (Stateless)
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                // URL별 접근 권한 설정
                .authorizeHttpRequests()
                // 모든 API 엔드포인트를 일시적으로 허용
                .antMatchers("/**").permitAll()
                .anyRequest().permitAll()
                .and()

                // HTTP Basic 인증 비활성화
                .httpBasic().disable()

                // Form 로그인 비활성화
                .formLogin().disable();

        // H2 콘솔을 위한 추가 설정
        http.headers().frameOptions().disable();

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 모든 도메인 허용 (개발 환경용)
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedOrigins(Arrays.asList("*"));

        // 모든 HTTP 메서드 허용
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));

        // 모든 헤더 허용
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 자격 증명 허용하지 않음 (allowCredentials와 allowedOrigins "*" 동시 사용 불가)
        configuration.setAllowCredentials(false);

        // Pre-flight 요청 캐시 시간
        configuration.setMaxAge(3600L);

        // 노출할 헤더
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}