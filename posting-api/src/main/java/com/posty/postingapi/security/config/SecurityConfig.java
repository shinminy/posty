package com.posty.postingapi.security.config;

import com.posty.postingapi.properties.ApiProperties;
import com.posty.postingapi.security.apikey.ApiKeyFilter;
import com.posty.postingapi.security.apikey.ApiKeyRepository;
import com.posty.postingapi.security.jwt.JwtAuthenticationFilter;
import com.posty.postingapi.security.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public ApiKeyFilter apiKeyFilter(ApiKeyRepository apiKeyRepository, ApiProperties apiProperties) {
        return new ApiKeyFilter(apiKeyRepository, apiProperties);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, ApiProperties apiProperties) {
        return new JwtAuthenticationFilter(jwtTokenProvider, apiProperties);
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            ApiKeyFilter apiKeyFilter,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
