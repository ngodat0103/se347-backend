package com.github.ngodat0103.se347_backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import java.net.URI;
import java.text.ParseException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@ConfigurationProperties("jwk")
@EnableMethodSecurity
public class SecurityConfiguration {
  private String allowedOrigin = "http://localhost:4200";

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, ObjectMapper objectMapper)
      throws Exception {
    this.configureCors(httpSecurity);
    this.configureCsrf(httpSecurity);
    this.configureResourceServer(httpSecurity, objectMapper);
    return httpSecurity.build();
  }

  private void configureCors(HttpSecurity http) throws Exception {
    http.cors(
        cors -> {
          CorsConfiguration configuration = new CorsConfiguration();
          configuration.setAllowedOriginPatterns(List.of(allowedOrigin));
          configuration.setAllowedHeaders(List.of("*"));
          configuration.setAllowedMethods(List.of("*"));
          UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
          source.registerCorsConfiguration("/**", configuration);
          cors.configurationSource(source);
        });
  }

  private void configureResourceServer(HttpSecurity httpSecurity, ObjectMapper objectMapper)
      throws Exception {
    httpSecurity.oauth2ResourceServer(
        resourceServerConfigurer -> {
          resourceServerConfigurer.jwt(Customizer.withDefaults());
          resourceServerConfigurer.authenticationEntryPoint(
              (request, response, authException) -> {
                ProblemDetail problemDetail =
                    ProblemDetail.forStatusAndDetail(
                        HttpStatus.UNAUTHORIZED, authException.getMessage());
                problemDetail.setTitle("Unauthorized");
                problemDetail.setType(
                    URI.create("https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/401"));
                problemDetail.setInstance(URI.create(request.getRequestURI()));
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                response.getWriter().write(objectMapper.writeValueAsString(problemDetail));
              });

          resourceServerConfigurer.accessDeniedHandler(
              (request, response, accessDeniedException) -> {
                ProblemDetail problemDetail =
                    ProblemDetail.forStatusAndDetail(
                        HttpStatus.FORBIDDEN, accessDeniedException.getMessage());
                problemDetail.setTitle("Forbidden");
                problemDetail.setType(
                    URI.create("https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/403"));
                problemDetail.setInstance(URI.create(request.getRequestURI()));
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                response.getWriter().write(objectMapper.writeValueAsString(problemDetail));
              });
        });
  }

  private void configureCsrf(HttpSecurity httpSecurity) throws Exception {
    httpSecurity.csrf(AbstractHttpConfigurer::disable);
  }

  @Bean
  @ConditionalOnProperty(name = "jwk.rsa.key-value")
  RSAKey rsaKey(@Value("${jwk.rsa.key-value}") String keyValue) throws ParseException {
    return RSAKey.parse(keyValue);
  }

  @Bean
  JwtEncoder jwtEncoder(JWK jwk) {
    JWKSet jwkSet = new JWKSet(jwk);
    ImmutableJWKSet<SecurityContext> immutableJWKSet = new ImmutableJWKSet<>(jwkSet);
    return new NimbusJwtEncoder(immutableJWKSet);
  }

  @Bean
  JwtDecoder jwtDecoder(RSAKey rsaKey) throws JOSEException {
    return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
