package com.github.ngodat0103.se347_backend.controller;

import com.github.ngodat0103.se347_backend.dto.account.CredentialDto;
import com.github.ngodat0103.se347_backend.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/api/v1/auth")
public class AuthController {

  private final AuthService authSvc;

  @Operation(summary = "Login", description = "Authenticate user and return access token")
  @ApiResponse(responseCode = "200", description = "Successful login")
  @PostMapping(path = "/login")
  public OAuth2AccessTokenResponse login(@RequestBody @Valid CredentialDto credentialDto) {
    return authSvc.login(credentialDto);
  }

  @Operation(summary = "Logout", description = "Logout user and blacklist token")
  @ApiResponse(responseCode = "200", description = "Successful logout")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("isAuthenticated()")
  @GetMapping(path = "/logout")
  public String logout(JwtAuthenticationToken jwtAuthenticationToken) {
    return this.authSvc.logout(jwtAuthenticationToken);
  }

  @Operation(summary = "Verify Email", description = "Verify user email with the provided code")
  @ApiResponse(responseCode = "200", description = "Email verified successfully")
  @GetMapping(path = "/verify-email")
  public String verifyEmail(@RequestParam String code) {
    return authSvc.verifyEmail(code);
  }

  @Operation(summary = "Resend Email Verification", description = "Resend email verification link")
  @ApiResponse(responseCode = "202", description = "Email verification link resent")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("isAuthenticated()")
  @GetMapping(path = "/resend-email")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public String resendEmailVerification(HttpServletRequest request) {
    return authSvc.resendEmailVerification(request);
  }

  @GetMapping(path = "/isValidJwt")
  @SecurityRequirement(name = "bearerAuth")
  @ResponseStatus(HttpStatus.ACCEPTED)
  @PreAuthorize("isAuthenticated()")
  public Map<String, Object> isValidJwt(JwtAuthenticationToken jwtAuthenticationToken) {
    return jwtAuthenticationToken.getTokenAttributes();
  }
}
