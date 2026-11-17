package com.mizanflow.worklog;
import jakarta.persistence.*; import lombok.*; import java.time.Instant; import java.util.UUID;
/** Historique immuable des transitions (qui, quand, de/vers quel statut, pourquoi). */
@Entity @Table(name="work_approvals") @Getter @Setter
public class WorkApproval { @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id; @Column(name="tenant_id", nullable=false) private UUID tenantId;
  @Column(name="work_log_id", nullable=false) private UUID workLogId; @Column(name="actor_id", nullable=false) private UUID actorId;
  @Column(name="from_status", nullable=false) private String fromStatus; @Column(name="to_status", nullable=false) private String toStatus; private String comment;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now(); }
