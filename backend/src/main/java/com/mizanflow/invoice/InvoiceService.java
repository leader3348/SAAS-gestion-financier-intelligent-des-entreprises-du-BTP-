package com.mizanflow.invoice;
import com.mizanflow.audit.AuditService; import com.mizanflow.common.Exceptions; import com.mizanflow.company.CompanyRepository; import com.mizanflow.customer.CustomerService; import com.mizanflow.project.ProjectService;
import com.mizanflow.security.CurrentUser; import com.mizanflow.subscription.PlanLimitService; import com.mizanflow.tenant.TenantContext; import com.mizanflow.worklog.*;
import jakarta.validation.constraints.*; import org.springframework.data.domain.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal; import java.time.*; import java.util.*;
@Service @Transactional
public class InvoiceService {
  public record FromWorkLogsRequest(@NotNull UUID projectId, @NotEmpty List<UUID> workLogIds, @NotNull @DecimalMin("0") BigDecimal vatRate, String paymentTerms) {}
  public record PaymentRequest(@NotNull @DecimalMin(value="0", inclusive=false) BigDecimal amount, @NotNull LocalDate paidOn, @NotNull Payment.Method method, String reference) {}
  public record InvoiceDto(UUID id, String number, String status, boolean overdue, UUID customerId, UUID projectId, LocalDate issueDate, LocalDate dueDate, BigDecimal totalHt, BigDecimal totalVat, BigDecimal totalTtc, BigDecimal paid, BigDecimal balance) {}
  public record PaymentDto(UUID id, UUID invoiceId, BigDecimal amount, LocalDate paidOn, Payment.Method method, String reference) {}
  private static final ZoneId MA = ZoneId.of("Africa/Casablanca");
  private final InvoiceRepository invoices; private final PaymentRepository payments; private final WorkLogRepository workLogs; private final ProjectService projects;
  private final CompanyRepository companies; private final PlanLimitService limits; private final AuditService audit; private final Clock clock;
  @org.springframework.beans.factory.annotation.Autowired
  public InvoiceService(InvoiceRepository i, PaymentRepository p, WorkLogRepository w, ProjectService pr, CompanyRepository c, PlanLimitService l, AuditService a){ this(i,p,w,pr,c,l,a,Clock.system(MA)); }
  InvoiceService(InvoiceRepository i, PaymentRepository p, WorkLogRepository w, ProjectService pr, CompanyRepository c, PlanLimitService l, AuditService a, Clock clock){ invoices=i; payments=p; workLogs=w; projects=pr; companies=c; limits=l; audit=a; this.clock=clock; }

  @Transactional(readOnly=true) public Page<InvoiceDto> list(Pageable p){ var today = LocalDate.now(clock); return invoices.findByTenantId(TenantContext.require(), p).map(i -> dto(i, today)); }
  @Transactional(readOnly=true) public InvoiceDto get(UUID id){ return dto(load(id), LocalDate.now(clock)); }

  /** Crée une facture BROUILLON à partir de prestations APPROUVÉES. Verrou pessimiste : deux requêtes concurrentes ne peuvent pas facturer deux fois la même quantité. */
  public InvoiceDto createFromWorkLogs(FromWorkLogsRequest r){
    UUID t = TenantContext.require(); limits.activePlan(t); var project = projects.load(r.projectId());
    var logs = workLogs.lockAll(new HashSet<>(r.workLogIds()), t);
    if (logs.size() != new HashSet<>(r.workLogIds()).size()) throw new Exceptions.NotFound("Prestation introuvable");
    var inv = new Invoice(); inv.setCustomerId(project.getCustomerId()); inv.setProjectId(project.getId()); inv.setPaymentTerms(r.paymentTerms());
    for (var w : logs) {
      if (!w.getProjectId().equals(project.getId())) throw new Exceptions.BusinessRule("Prestation d'un autre projet");
      if (w.getStatus() != WorkLog.Status.APPROVED) throw new Exceptions.BusinessRule("Seules les prestations APPROUVÉES sont facturables (statut actuel : " + w.getStatus() + ")");
      var remaining = w.remainingQuantity(); if (remaining.signum() <= 0) throw new Exceptions.BusinessRule("Prestation déjà entièrement facturée");
      var l = new InvoiceLine(); l.setLabel(w.getServiceType()); l.setQuantity(remaining); l.setUnit(w.getUnit().name()); l.setUnitPrice(w.getUnitPrice()); l.setVatRate(r.vatRate()); l.setWorkLogId(w.getId()); inv.addLine(l);
      w.setInvoicedQuantity(w.getQuantity()); w.transitionTo(WorkLog.Status.INVOICED);
    }
    inv.recomputeTotals(); inv = invoices.save(inv);
    audit.log(t, CurrentUser.get().id(), "INVOICE_CREATED", "Invoice", inv.getId(), "work-logs=" + logs.size());
    return dto(inv, LocalDate.now(clock)); }

  public InvoiceDto issue(UUID id, int paymentDays){
    UUID t = TenantContext.require(); var inv = invoices.lockByIdAndTenant(id, t).orElseThrow(() -> new Exceptions.NotFound("Facture introuvable"));
    companies.lockById(t); // sérialise la numérotation par entreprise
    var today = LocalDate.now(clock); String prefix = "FAC-" + today.getYear() + "-";
    inv.issue(prefix + String.format("%04d", invoices.countNumbered(t, prefix) + 1), today, today.plusDays(paymentDays));
    audit.log(t, CurrentUser.get().id(), "INVOICE_ISSUED", "Invoice", id, inv.getNumber()); return dto(inv, today); }

  public InvoiceDto cancel(UUID id, String reason){
    var inv = invoices.lockByIdAndTenant(id, TenantContext.require()).orElseThrow(() -> new Exceptions.NotFound("Facture introuvable"));
    inv.cancel(reason); audit.log(inv.getTenantId(), CurrentUser.get().id(), "INVOICE_CANCELLED", "Invoice", id, reason); return dto(inv, LocalDate.now(clock)); }

  /** Enregistre un paiement RÉEL saisi par un utilisateur habilité. La facture est verrouillée (ligne) pour éviter les dépassements concurrents. */
  public PaymentDto recordPayment(UUID invoiceId, PaymentRequest r){
    UUID t = TenantContext.require(); var inv = invoices.lockByIdAndTenant(invoiceId, t).orElseThrow(() -> new Exceptions.NotFound("Facture introuvable"));
    if (r.paidOn().isAfter(LocalDate.now(clock))) throw new Exceptions.BusinessRule("Date de paiement dans le futur");
    inv.applyPayment(r.amount());
    var p = new Payment(); p.setInvoiceId(invoiceId); p.setAmount(r.amount()); p.setPaidOn(r.paidOn()); p.setMethod(r.method()); p.setReference(r.reference()); p.setRecordedBy(CurrentUser.get().id());
    p = payments.save(p); audit.log(t, CurrentUser.get().id(), "PAYMENT_RECORDED", "Payment", p.getId(), r.amount() + " MAD"); return pdto(p); }

  @Transactional(readOnly=true) public Page<PaymentDto> payments(Pageable p){ return payments.findByTenantId(TenantContext.require(), p).map(InvoiceService::pdto); }
  private Invoice load(UUID id){ return invoices.findByIdAndTenantId(id, TenantContext.require()).orElseThrow(() -> new Exceptions.NotFound("Facture introuvable")); }
  static InvoiceDto dto(Invoice i, LocalDate today){ boolean od = i.isOverdue(today);
    return new InvoiceDto(i.getId(), i.getNumber(), od ? "OVERDUE" : i.getStatus().name(), od, i.getCustomerId(), i.getProjectId(), i.getIssueDate(), i.getDueDate(), i.getTotalHt(), i.getTotalVat(), i.getTotalTtc(), i.getPaidAmount(), i.balance()); }
  static PaymentDto pdto(Payment p){ return new PaymentDto(p.getId(), p.getInvoiceId(), p.getAmount(), p.getPaidOn(), p.getMethod(), p.getReference()); }
}
