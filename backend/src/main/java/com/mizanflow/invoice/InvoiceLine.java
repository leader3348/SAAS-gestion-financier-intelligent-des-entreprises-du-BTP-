package com.mizanflow.invoice;
import jakarta.persistence.*; import lombok.*; import java.math.BigDecimal; import java.util.UUID;
@Entity @Table(name="invoice_lines") @Getter @Setter
public class InvoiceLine { @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
  @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="invoice_id") private Invoice invoice;
  private int position; private String label; private BigDecimal quantity; private String unit; @Column(name="unit_price") private BigDecimal unitPrice;
  @Column(name="discount_pct") private BigDecimal discountPct = BigDecimal.ZERO; @Column(name="vat_rate") private BigDecimal vatRate = new BigDecimal("20"); @Column(name="work_log_id") private UUID workLogId; }
