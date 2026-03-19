package com.team1.hrbank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

  @Bean
  public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();

    config.setAllowCredentials(true);
    config.addAllowedOriginPattern("https://sb09-hrbank-team01-production-6d26.up.railway.app"); // 3번 참고: 가급적 실제 URL 명시 권장
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");

    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }
}