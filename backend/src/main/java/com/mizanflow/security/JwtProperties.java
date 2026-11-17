package com.mizanflow.security;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix="mizanflow.jwt")
public record JwtProperties(String secret, long accessTtlMinutes, long refreshTtlDays) {
  public JwtProperties { if (secret == null || secret.length() < 32) throw new IllegalStateException("JWT_SECRET doit contenir au moins 32 caractères"); }
}
