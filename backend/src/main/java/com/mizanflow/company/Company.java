package com.mizanflow.company;
import jakarta.persistence.*; import lombok.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="companies") @Getter @Setter
public class Company {
  public enum Status { ACTIVE, SUSPENDED }
  @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
  @Column(nullable=false) private String name;
  @Column(name="legal_name") private String legalName;
  private String ice; private String rc; @Column(name="tax_id") private String taxId;
  private String address; private String city; private String phone; private String email;
  @Column(nullable=false) private String currency = "MAD";
  @Column(nullable=false) private String language = "fr";
  @Enumerated(EnumType.STRING) @Column(nullable=false) private Status status = Status.ACTIVE;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
}
