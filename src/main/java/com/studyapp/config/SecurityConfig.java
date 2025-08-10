package com.studyapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (API 서버이므로)
                .csrf().disable()

                // CORS 설정
                .cors().and()

                // 세션 사용하지 않음 (Stateless)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()

                // URL별 접근 권한 설정 (Spring Boot 2.7 방식)
                .authorizeHttpRequests()
                // 인증 없이 접근 가능한 URL들
                .antMatchers("/api/auth/**").permitAll()        // 로그인, 회원가입
                .antMatchers("/api/study-groups/**").permitAll() // 스터디 그룹
                .antMatchers("/api/attendance/**").permitAll()   // 출석
                .antMatchers("/h2-console/**").permitAll()       // H2 콘솔
                .antMatchers("/error").permitAll()              // 에러 페이지
                .antMatchers("/").permitAll()                   // 루트 경로

                // 나머지 모든 요청은 인증 필요 (나중에 JWT로 변경 예정)
                .anyRequest().authenticated()

                .and()

                // HTTP Basic 인증 비활성화
                .httpBasic().disable()

                // Form 로그인 비활성화
                .formLogin().disable();

        // H2 콘솔을 위한 추가 설정
        http.headers().frameOptions().disable();

        return http.build();
    }
}