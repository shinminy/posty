package com.posty.postingapi.security.config;

import com.posty.postingapi.properties.ApiProperties;
import com.posty.postingapi.security.apikey.ApiKeyFilter;
import com.posty.postingapi.security.apikey.ApiKeyRepository;
import com.posty.postingapi.security.jwt.JwtAuthenticationFilter;
import com.posty.postingapi.security.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public ApiKeyFilter apiKeyFilter(
            ApiKeyRepository apiKeyRepository,
            CustomAuthenticationEntryPoint authenticationEntryPoint,
            ApiProperties apiProperties
    ) {
        return new ApiKeyFilter(
                apiKeyRepository,
                authenticationEntryPoint,
                apiProperties
        );
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            CustomAuthenticationEntryPoint authenticationEntryPoint,
            ApiProperties apiProperties
    ) {
        return new JwtAuthenticationFilter(
                jwtTokenProvider,
                authenticationEntryPoint,
                apiProperties
        );
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            ApiKeyFilter apiKeyFilter,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomAuthenticationEntryPoint authenticationEntryPoint
    ) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
