package com.posty.fileapi.security;

import com.posty.fileapi.common.AuthConstants;
import com.posty.fileapi.properties.ApiConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class SimpleTokenAuthenticationFilter extends OncePerRequestFilter {

    private final String validToken;

    public SimpleTokenAuthenticationFilter(ApiConfig apiConfig) {
        validToken = apiConfig.getToken();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader(AuthConstants.AUTH_HEADER);

        if (token != null && token.startsWith(AuthConstants.BEARER_PREFIX)) {
            String actualToken = token.substring(AuthConstants.BEARER_PREFIX.length());
            if (validToken.equals(actualToken)) {
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        "fixedUser", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
