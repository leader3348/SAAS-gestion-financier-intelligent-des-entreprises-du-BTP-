package com.mizanflow.subscription;
import jakarta.persistence.*; import lombok.*; import java.math.BigDecimal;
@Entity @Table(name="plans") @Getter @Setter
public class Plan { @Id private String code; private String name;
  @Column(name="price_monthly") private BigDecimal priceMonthly; @Column(name="price_yearly") private BigDecimal priceYearly;
  @Column(name="max_users") private int maxUsers; @Column(name="max_active_projects") private int maxActiveProjects; private String features; private boolean active; }
