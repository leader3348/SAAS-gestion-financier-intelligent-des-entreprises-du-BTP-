package com.mizanflow.audit;
import jakarta.persistence.*; import lombok.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="audit_logs") @Getter @Setter
public class AuditLog { @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id; @Column(name="tenant_id") private UUID tenantId; @Column(name="actor_id") private UUID actorId;
  private String action; @Column(name="entity_type") private String entityType; @Column(name="entity_id") private UUID entityId; private String details;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now(); }
