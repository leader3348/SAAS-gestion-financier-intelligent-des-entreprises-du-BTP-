package com.mizanflow.auth;
import jakarta.persistence.*; import lombok.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="refresh_tokens") @Getter @Setter
public class RefreshToken { @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id; @Column(name="user_id", nullable=false) private UUID userId;
  @Column(name="token_hash", nullable=false, unique=true) private String tokenHash; @Column(name="expires_at", nullable=false) private Instant expiresAt; private boolean revoked;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now(); }
