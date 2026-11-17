package com.mizanflow.invoice;
import com.mizanflow.common.*; import jakarta.persistence.*; import lombok.*; import java.math.BigDecimal; import java.time.LocalDate; import java.util.*;
@Entity @Table(name="invoices") @Getter @Setter
public class Invoice extends TenantEntity {
  public enum Status { DRAFT, ISSUED, PARTIALLY_PAID, PAID, CANCELLED }
  private String number; @Column(name="customer_id", nullable=false) private UUID customerId; @Column(name="project_id") private UUID projectId;
  @Enumerated(EnumType.STRING) @Column(nullable=false) private Status status = Status.DRAFT;
  @Column(name="issue_date") private LocalDate issueDate; @Column(name="due_date") private LocalDate dueDate; @Column(name="payment_terms") private String paymentTerms;
  @Column(name="total_ht", nullable=false) private BigDecimal totalHt = BigDecimal.ZERO; @Column(name="total_vat", nullable=false) private BigDecimal totalVat = BigDecimal.ZERO;
  @Column(name="total_ttc", nullable=false) private BigDecimal totalTtc = BigDecimal.ZERO; @Column(name="paid_amount", nullable=false) private BigDecimal paidAmount = BigDecimal.ZERO;
  @Column(name="cancel_reason") private String cancelReason;
  @OneToMany(mappedBy="invoice", cascade=CascadeType.ALL, orphanRemoval=true) @OrderBy("position") private List<InvoiceLine> lines = new ArrayList<>();

  public BigDecimal balance(){ return totalTtc.subtract(paidAmount); }
  /** OVERDUE est dérivé : émise/partiellement payée, échéance dépassée et solde > 0. */
  public boolean isOverdue(LocalDate today){ return (status == Status.ISSUED || status == Status.PARTIALLY_PAID) && dueDate != null && dueDate.isBefore(today) && balance().signum() > 0; }
  public void addLine(InvoiceLine l){ l.setInvoice(this); l.setPosition(lines.size() + 1); lines.add(l); }
  public void recomputeTotals(){
    BigDecimal ht = BigDecimal.ZERO, vat = BigDecimal.ZERO;
    for (var l : lines){ var h = Money.lineHt(l.getQuantity(), l.getUnitPrice(), l.getDiscountPct()); ht = ht.add(h); vat = vat.add(Money.vat(h, l.getVatRate())); }
    totalHt = ht; totalVat = vat; totalTtc = ht.add(vat); }
  public void issue(String number, LocalDate issued, LocalDate due){
    if (status != Status.DRAFT) throw new Exceptions.BusinessRule("Seule une facture brouillon peut être émise");
    if (lines.isEmpty()) throw new Exceptions.BusinessRule("Facture sans ligne");
    if (due.isBefore(issued)) throw new Exceptions.BusinessRule("Échéance antérieure à l'émission");
    this.number = number; this.issueDate = issued; this.dueDate = due; this.status = Status.ISSUED; }
  /** Applique un paiement CONFIRMÉ. Le statut PAID n'est atteint que lorsque le solde est exactement nul. */
  public void applyPayment(BigDecimal amount){
    if (status != Status.ISSUED && status != Status.PARTIALLY_PAID) throw new Exceptions.BusinessRule("Cette facture n'accepte pas de paiement (" + status + ")");
    if (amount.signum() <= 0) throw new Exceptions.BusinessRule("Montant invalide");
    if (amount.compareTo(balance()) > 0) throw new Exceptions.BusinessRule("Le paiement dépasse le solde restant (" + balance() + ")");
    paidAmount = paidAmount.add(amount); status = balance().signum() == 0 ? Status.PAID : Status.PARTIALLY_PAID; }
  public void cancel(String reason){
    if (reason == null || reason.isBlank()) throw new Exceptions.BusinessRule("Motif d'annulation obligatoire");
    if (paidAmount.signum() > 0) throw new Exceptions.BusinessRule("Facture déjà (partiellement) encaissée : établir un avoir/une correction");
    if (status == Status.CANCELLED) throw new Exceptions.BusinessRule("Déjà annulée"); status = Status.CANCELLED; cancelReason = reason; }
}
