package com.github.ngodat0103.usersvc.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import java.net.URI;
import java.text.ParseException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import reactor.core.publisher.Flux;

@Slf4j
@Configuration
@EnableReactiveMethodSecurity
@EnableWebFluxSecurity
@ConfigurationProperties("jwk")
public class SecurityConfiguration {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  SecurityWebFilterChain httpSecurity(
      ServerHttpSecurity http,
      CustomJwtAuthenticationManager customJwtAuthenticationManager,
      ObjectMapper objectMapper) {
    configureCors(http);
    configureCsrf(http);
    configureAuthorizeExchange(http);
    configureOauth2ResourceServer(http, objectMapper, customJwtAuthenticationManager);
    configureExceptionHandling(http);
    return http.build();
  }

  private void configureCors(ServerHttpSecurity httpSecurity) {

    httpSecurity.cors(
        cors ->
            cors.configurationSource(
                request -> {
                  var corsConfiguration = new org.springframework.web.cors.CorsConfiguration();
                  corsConfiguration.addAllowedOrigin("http://localhost:4200");
                  corsConfiguration.addAllowedHeader("*");
                  corsConfiguration.addAllowedMethod("*");
                  return corsConfiguration;
                }));
  }

  private void configureCsrf(ServerHttpSecurity httpSecurity) {
    httpSecurity.csrf(ServerHttpSecurity.CsrfSpec::disable);
  }

  private void configureAuthorizeExchange(ServerHttpSecurity httpSecurity) {
    httpSecurity.authorizeExchange(exchanges -> exchanges.anyExchange().permitAll());
  }

  private void configureOauth2ResourceServer(
      ServerHttpSecurity httpSecurity,
      ObjectMapper objectMapper,
      CustomJwtAuthenticationManager customJwtAuthenticationManager) {

    httpSecurity.oauth2ResourceServer(
        resourceServer -> {
          resourceServer.jwt(jwt -> jwt.authenticationManager(customJwtAuthenticationManager));

          resourceServer.accessDeniedHandler(
              (exchange, denied) -> {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.FORBIDDEN);
                response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
                ProblemDetail problemDetail =
                    ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, denied.getMessage());
                problemDetail.setType(
                    URI.create("https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/403"));
                DataBuffer dataBuffer;
                try {
                  dataBuffer =
                      response.bufferFactory().wrap(objectMapper.writeValueAsBytes(problemDetail));
                } catch (JsonProcessingException e) {
                  throw new RuntimeException(e);
                }
                return response.writeWith(Flux.just(dataBuffer));
              });

          resourceServer.authenticationEntryPoint(
              (exchange, ex) -> {
                ServerHttpResponse serverHttpResponse = exchange.getResponse();
                serverHttpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
                serverHttpResponse.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
                ProblemDetail problemDetail =
                    ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
                problemDetail.setType(
                    URI.create("https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/401"));
                DataBuffer dataBuffer;
                try {
                  dataBuffer =
                      serverHttpResponse
                          .bufferFactory()
                          .wrap(objectMapper.writeValueAsBytes(problemDetail));
                } catch (JsonProcessingException e) {
                  throw new RuntimeException(e);
                }
                return serverHttpResponse.writeWith(Flux.just(dataBuffer));
              });
        });
  }

  private void configureExceptionHandling(ServerHttpSecurity httpSecurity) {
    httpSecurity.exceptionHandling(
        exceptionHandlingSpec ->
            exceptionHandlingSpec.authenticationEntryPoint(
                new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)));
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
  ReactiveJwtDecoder jwtDecoder(RSAKey rsaKey) throws JOSEException {
    return NimbusReactiveJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
  }
}
