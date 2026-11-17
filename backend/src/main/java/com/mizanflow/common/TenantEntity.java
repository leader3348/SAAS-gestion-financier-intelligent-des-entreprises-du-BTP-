package com.mizanflow.common;
import com.mizanflow.tenant.TenantContext;
import jakarta.persistence.*; import lombok.Getter; import lombok.Setter; import java.time.Instant; import java.util.UUID;
/** Toute entité métier hérite de ce socle : tenant_id immuable + garde-fou anti inter-tenant à l'écriture. */
@MappedSuperclass @Getter @Setter
public abstract class TenantEntity {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @Column(name="tenant_id", nullable=false, updatable=false) private UUID tenantId;
  @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt;
  @Column(name="updated_at", nullable=false) private Instant updatedAt;
  @Version private long version;
  @PrePersist void prePersist(){
    UUID ctx = TenantContext.get().orElse(null);
    if (tenantId == null) tenantId = ctx;
    if (tenantId == null) throw new IllegalStateException("tenant_id manquant");
    if (ctx != null && !ctx.equals(tenantId)) throw new IllegalStateException("Écriture inter-tenant interdite");
    createdAt = updatedAt = Instant.now(); }
  @PreUpdate void preUpdate(){
    UUID ctx = TenantContext.get().orElse(null);
    if (ctx != null && !ctx.equals(tenantId)) throw new IllegalStateException("Modification inter-tenant interdite");
    updatedAt = Instant.now(); }
}
