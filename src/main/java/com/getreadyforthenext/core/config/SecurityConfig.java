package com.getreadyforthenext.core.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${oauth.successUrl}")
    String successUrl;

    @Value("${oauth.failureUrl}")
    String failureUrl;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        httpSecurity.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        httpSecurity.oauth2Login(oauth2LoginCustomizer -> oauth2LoginCustomizer.loginPage("/"));
        httpSecurity.oauth2Login(oauth2LoginCustomizer -> oauth2LoginCustomizer.userInfoEndpoint(userInfo -> userInfo.userService(this.oAuth2UserService())));
        httpSecurity.oauth2Login(oauth2LoginCustomizer -> oauth2LoginCustomizer.successHandler(this::successHandler));
        httpSecurity.oauth2Login(oauth2LoginCustomizer -> oauth2LoginCustomizer.failureHandler(this::failureHandler));

        httpSecurity.authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests.requestMatchers("/").permitAll());
        httpSecurity.authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests.requestMatchers("/oauth2/authorization/google").permitAll());
        httpSecurity.authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests.requestMatchers("/actuator/**").hasRole("ADMIN"));
        httpSecurity.authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests.anyRequest().authenticated());

        return httpSecurity.build();
    }

    @Bean
    public DefaultOAuth2UserService oAuth2UserService() {
        return new DefaultOAuth2UserService();
    }

    private void successHandler(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        System.out.println((String) oauth2User.getAttribute("name"));
        System.out.println((String) oauth2User.getAttribute("email"));

        response.sendRedirect(this.successUrl + "?accessToken=" + "token");
    }

    private void failureHandler(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        response.sendRedirect(this.failureUrl);
    }

    @Bean
    public PasswordEncoder PasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
