package com.mizanflow.customer;
import com.mizanflow.common.TenantEntity; import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="customers") @Getter @Setter
public class Customer extends TenantEntity { @Column(nullable=false) private String name; private String ice, email, phone, address, city, notes; }
