package com.web.billim.security.config;

import com.web.billim.jwt.filter.JwtAuthenticationFilter;
import com.web.billim.jwt.filter.JwtExceptionFilter;
import com.web.billim.jwt.provider.JwtProvider;
import com.web.billim.jwt.service.JwtService;
import com.web.billim.oauth.CustomOAuthTokenResponseConverter;
import com.web.billim.security.filter.LoginAuthenticationFilter;
import com.web.billim.security.provider.UsernamPasswordAuthenticationProvider;

import com.web.billim.security.service.UserDetailServiceImpl;

import com.web.billim.oauth.OAuth2LoginSuccessHandler;
import com.web.billim.oauth.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtProvider jwtProvider;
    private final UserDetailServiceImpl userDetailsService;
    private final JwtService jwtService;
    private final SecurityFilterSkipMatcher securityFilterSkipMatcher;
    private final OAuthService oauthService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain securityFilterChain(AuthenticationManager authenticationManager, HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()

                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                .authorizeRequests()
                .antMatchers("/**").permitAll()
                .anyRequest().authenticated()

                .and()
                .apply(jwtTokenFilterConfigurer(jwtProvider, authenticationManager, jwtService, securityFilterSkipMatcher))

                .and()
                .oauth2Login()
//                .authorizationEndpoint().authorizationRequestRepository(new CustomRepository())
//                .and()
                .tokenEndpoint().accessTokenResponseClient(accessTokenResponseClient())
                .and().redirectionEndpoint().baseUri("/oauth/kakao")
                .and().userInfoEndpoint().userService(oauthService)
                .and().successHandler(oAuth2LoginSuccessHandler);

        return http.build();
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient authorizationCodeTokenResponseClient = new DefaultAuthorizationCodeTokenResponseClient();

        OAuth2AccessTokenResponseHttpMessageConverter converter = new OAuth2AccessTokenResponseHttpMessageConverter();
        converter.setAccessTokenResponseConverter(new CustomOAuthTokenResponseConverter());

        RestTemplate restTemplate = new RestTemplate(Arrays.asList(new FormHttpMessageConverter(), converter));
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        authorizationCodeTokenResponseClient.setRestOperations(restTemplate);
        return authorizationCodeTokenResponseClient;
    }

    @Bean
    public AuthenticationManager configureAuthenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(usernamPasswordAuthenticationProvider());
//        authenticationManagerBuilder.authenticationProvider(jwtAuthenticationProvider());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterConfigurer jwtTokenFilterConfigurer(
            JwtProvider jwtProvider,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            SecurityFilterSkipMatcher securityFilterSkipMatcher
    ) {
        return new SecurityFilterConfigurer(jwtProvider, authenticationManager, jwtService, securityFilterSkipMatcher);
    }

    @Bean
    public JwtExceptionFilter jwtExceptionFilter() {
        return new JwtExceptionFilter(securityFilterSkipMatcher);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtProvider jwtProvider) {
        return new JwtAuthenticationFilter(jwtProvider, securityFilterSkipMatcher, jwtService);
    }

    @Bean
    public LoginAuthenticationFilter loginAuthenticationFilter(
            AuthenticationManager configureAuthenticationManager,
            JwtProvider jwtProvider,
            JwtService jwtService
    ) {
        LoginAuthenticationFilter loginAuthenticationFilter = new LoginAuthenticationFilter(configureAuthenticationManager, jwtProvider, jwtService);
        loginAuthenticationFilter.setAuthenticationManager(configureAuthenticationManager);
        return loginAuthenticationFilter;
    }

    @Bean
    public UsernamPasswordAuthenticationProvider usernamPasswordAuthenticationProvider() {
        return new UsernamPasswordAuthenticationProvider(userDetailsService, passwordEncoder);
    }

}
