package com.ssafy.goose.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 도메인 (여기에 프론트엔드 도메인 추가)
        configuration.setAllowedOrigins(List.of(
                "http://localhost:8080",
                "http://i12d208.p.ssafy.io:8090"
        ));

        // 모든 도메인 허용 (setAllowedOrigins와 충돌 방지)
        configuration.setAllowedOriginPatterns(List.of("*"));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 허용할 HTTP 헤더
        configuration.setAllowedHeaders(List.of("*"));

        // 클라이언트가 응답에서 접근할 수 있도록 허용할 헤더
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));

        // 클라이언트에서 인증 정보 포함 가능 여부
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
