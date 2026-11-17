package com.mizanflow.subscription;
import jakarta.persistence.*; import lombok.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="subscriptions") @Getter @Setter
public class Subscription {
  public enum Status { TRIAL, ACTIVE, EXPIRED, SUSPENDED, CANCELLED }
  @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
  @Column(name="tenant_id", nullable=false) private UUID tenantId;
  @Column(name="plan_code", nullable=false) private String planCode;
  @Column(name="billing_cycle", nullable=false) private String billingCycle = "MONTHLY";
  @Enumerated(EnumType.STRING) @Column(nullable=false) private Status status;
  @Column(name="trial_ends_at") private Instant trialEndsAt; @Column(name="period_start") private Instant periodStart; @Column(name="period_end") private Instant periodEnd;
  @Column(name="payment_proof_ref") private String paymentProofRef; @Column(name="validated_by") private UUID validatedBy;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now(); @Column(name="updated_at", nullable=false) private Instant updatedAt = Instant.now();
  @Version private long version;
  /** Statut réel : un essai ou une période dépassés sont considérés expirés même si le statut stocké n'a pas encore été mis à jour. */
  public boolean isUsable(Instant now){
    return switch (status) { case TRIAL -> trialEndsAt != null && now.isBefore(trialEndsAt); case ACTIVE -> periodEnd != null && now.isBefore(periodEnd); default -> false; }; }
}
