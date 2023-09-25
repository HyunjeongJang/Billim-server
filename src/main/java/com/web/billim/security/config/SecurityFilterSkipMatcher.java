package com.web.billim.security.config;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Component
public class SecurityFilterSkipMatcher{

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final String[] excludedPaths = {
            "/image-delete-test",
            "/", "/ping",
            "/email/**",
            "/member/email/**",
            "/member/signup",
            "/member/check/nickname",
            "/member/find/password",
            "/product/list/search", "/product/list/most/popular", "/product/detail/**",
            "/review/list/**",
            "/auth/reIssue/token",
            "/subscribe/**", "/publish/**", "/stomp/**",
            "/oauth/kakao", "/oauth2/authorization/kakao", "/login",

            "/v3/api-docs", "/configuration/ui", "/swagger-resources/**",
            "/configuration/security", "/swagger-ui.html/**", "/swagger-ui/**", "/webjars/**", "/swagger/**",
            "/v3/api-docs/swagger-config"
    };

    public boolean shouldSkipFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return Arrays.stream(excludedPaths)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

}
