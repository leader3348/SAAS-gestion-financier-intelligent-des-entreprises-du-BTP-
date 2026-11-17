package com.mizanflow.worklog;
import com.mizanflow.common.TenantEntity; import com.mizanflow.common.Exceptions; import jakarta.persistence.*; import lombok.*; import java.math.BigDecimal; import java.time.LocalDate; import java.util.*;
@Entity @Table(name="work_logs") @Getter @Setter
public class WorkLog extends TenantEntity {
  public enum Status { DRAFT, SUBMITTED, APPROVED, REJECTED, INVOICED;
    private static final Map<Status, Set<Status>> ALLOWED = Map.of(DRAFT, Set.of(SUBMITTED), SUBMITTED, Set.of(APPROVED, REJECTED), REJECTED, Set.of(SUBMITTED), APPROVED, Set.of(INVOICED), INVOICED, Set.of());
    public boolean canGoTo(Status n){ return ALLOWED.getOrDefault(this, Set.of()).contains(n); } }
  public enum Unit { M2, M3, ML, UNIT, HOUR, DAY }
  @Column(name="project_id", nullable=false) private UUID projectId; @Column(name="author_id", nullable=false) private UUID authorId;
  @Column(name="work_date", nullable=false) private LocalDate workDate; @Column(name="service_type", nullable=false) private String serviceType; private String description;
  @Column(nullable=false) private BigDecimal quantity; @Enumerated(EnumType.STRING) @Column(nullable=false) private Unit unit;
  @Column(name="unit_price", nullable=false) private BigDecimal unitPrice = BigDecimal.ZERO; @Column(name="invoiced_quantity", nullable=false) private BigDecimal invoicedQuantity = BigDecimal.ZERO;
  @Enumerated(EnumType.STRING) @Column(nullable=false) private Status status = Status.DRAFT; @Column(name="rejection_reason") private String rejectionReason;
  public BigDecimal remainingQuantity(){ return quantity.subtract(invoicedQuantity); }
  public void transitionTo(Status n){ if (!status.canGoTo(n)) throw new Exceptions.BusinessRule("Transition interdite : " + status + " → " + n); status = n; }
}
