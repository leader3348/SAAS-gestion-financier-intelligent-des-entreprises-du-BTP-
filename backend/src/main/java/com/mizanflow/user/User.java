package com.mizanflow.user;
import jakarta.persistence.*; import lombok.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="users") @Getter @Setter
public class User {
  @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
  @Column(name="tenant_id") private UUID tenantId; // null uniquement pour SUPER_ADMIN
  @Column(nullable=false) private String email;
  @Column(name="password_hash", nullable=false) private String passwordHash;
  @Column(name="full_name", nullable=false) private String fullName;
  @Enumerated(EnumType.STRING) @Column(nullable=false) private Role role;
  @Column(nullable=false) private boolean active = true;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
}
