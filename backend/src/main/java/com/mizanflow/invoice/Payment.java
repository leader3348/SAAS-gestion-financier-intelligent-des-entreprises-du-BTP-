package com.mizanflow.invoice;
import com.mizanflow.common.TenantEntity; import jakarta.persistence.*; import lombok.*; import java.math.BigDecimal; import java.time.LocalDate; import java.util.UUID;
@Entity @Table(name="payments") @Getter @Setter
public class Payment extends TenantEntity { public enum Method { TRANSFER, CASH, CHECK, CARD, OTHER }
  @Column(name="invoice_id", nullable=false) private UUID invoiceId; @Column(nullable=false) private BigDecimal amount; @Column(name="paid_on", nullable=false) private LocalDate paidOn;
  @Enumerated(EnumType.STRING) @Column(nullable=false) private Method method; private String reference; @Column(name="recorded_by", nullable=false) private UUID recordedBy; }
