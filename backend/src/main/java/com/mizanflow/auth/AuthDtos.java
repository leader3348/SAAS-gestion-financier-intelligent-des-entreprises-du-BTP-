package com.mizanflow.auth;
import jakarta.validation.constraints.*;
public final class AuthDtos { private AuthDtos(){}
  public record RegisterRequest(@NotBlank @Size(max=160) String companyName, @NotBlank @Size(max=120) String fullName, @Email @NotBlank String email,
    @NotBlank @Size(min=12, max=100, message="12 caractères minimum") String password) {}
  public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
  public record RefreshRequest(@NotBlank String refreshToken) {}
  public record TokenResponse(String accessToken, String refreshToken, long expiresInSeconds, String role, String fullName) {}
  public record WebTokenResponse(String accessToken, long expiresInSeconds, String role, String fullName) {
    public static WebTokenResponse from(TokenResponse tokens) {
      return new WebTokenResponse(tokens.accessToken(), tokens.expiresInSeconds(), tokens.role(), tokens.fullName());
    }
  }
}
