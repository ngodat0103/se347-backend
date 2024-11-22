package com.github.ngodat0103.usersvc.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;

@Configuration
@EnableReactiveMethodSecurity
@EnableWebFluxSecurity
public class SecurityConfiguration {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  SecurityWebFilterChain httpSecurity(ServerHttpSecurity http) {
    configureCors(http);
    configureCsrf(http);
    configureAuthorizeExchange(http);
    configureOauth2ResourceServer(http);
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

  private void configureOauth2ResourceServer(ServerHttpSecurity httpSecurity) {
    httpSecurity.oauth2ResourceServer(resource -> resource.jwt(Customizer.withDefaults()));
  }

  private void configureExceptionHandling(ServerHttpSecurity httpSecurity) {
    httpSecurity.exceptionHandling(
        exceptionHandlingSpec ->
            exceptionHandlingSpec
                .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
                .accessDeniedHandler(
                    (exchange, denied) -> {
                      exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                      return exchange.getResponse().setComplete();
                    }));
  }

  @Bean
  @ConditionalOnMissingBean
  RSAKey rsaKeyAutoGenerate() throws JOSEException {
    RSAKeyGenerator rsaKeyGenerator = new RSAKeyGenerator(4096);
    return rsaKeyGenerator.generate();
  }

  //  @Bean
  //  @ConditionalOnProperty(name = "jwk.rsa.key-value")
  //  RSAKey rsaKey(String keyValue) throws JOSEException {
  //     RSAKey rsaKey = RSAKey.parse()
  //  }

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
