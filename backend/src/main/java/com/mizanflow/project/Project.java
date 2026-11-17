package com.mizanflow.project;
import com.mizanflow.common.TenantEntity; import jakarta.persistence.*; import lombok.*; import java.math.BigDecimal; import java.time.LocalDate; import java.util.UUID;
@Entity @Table(name="projects") @Getter @Setter
public class Project extends TenantEntity {
  public enum Status { DRAFT, PLANNED, IN_PROGRESS, ON_HOLD, COMPLETED, CANCELLED }
  @Column(nullable=false) private String reference; @Column(nullable=false) private String name; @Column(name="customer_id", nullable=false) private UUID customerId;
  private String description; @Column(name="site_address") private String siteAddress; @Column(name="manager_id") private UUID managerId;
  @Column(name="start_date") private LocalDate startDate; @Column(name="planned_end_date") private LocalDate plannedEndDate;
  @Column(nullable=false) private BigDecimal budget = BigDecimal.ZERO; @Enumerated(EnumType.STRING) @Column(nullable=false) private Status status = Status.DRAFT; }
