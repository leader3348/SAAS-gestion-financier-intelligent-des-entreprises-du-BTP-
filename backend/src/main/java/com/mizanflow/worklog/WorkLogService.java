package com.mizanflow.worklog;
import com.mizanflow.audit.AuditService; import com.mizanflow.common.Exceptions; import com.mizanflow.project.ProjectService; import com.mizanflow.security.*; import com.mizanflow.tenant.TenantContext; import com.mizanflow.user.Role;
import jakarta.validation.constraints.*; import org.springframework.data.domain.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.math.BigDecimal; import java.time.LocalDate; import java.util.UUID;
@Service @Transactional
public class WorkLogService {
  public record WorkLogRequest(@NotNull UUID projectId, @NotNull LocalDate workDate, @NotBlank String serviceType, String description, @NotNull @DecimalMin(value="0", inclusive=false) BigDecimal quantity, @NotNull WorkLog.Unit unit, @NotNull @DecimalMin("0") BigDecimal unitPrice) {}
  public record WorkLogDto(UUID id, UUID projectId, UUID authorId, LocalDate workDate, String serviceType, String description, BigDecimal quantity, WorkLog.Unit unit, BigDecimal unitPrice, BigDecimal invoicedQuantity, WorkLog.Status status, String rejectionReason) {}
  private final WorkLogRepository repo; private final WorkApprovalRepository approvals; private final ProjectService projects; private final AuditService audit;
  public WorkLogService(WorkLogRepository r, WorkApprovalRepository a, ProjectService p, AuditService au){ repo=r; approvals=a; projects=p; audit=au; }
  @Transactional(readOnly=true) public Page<WorkLogDto> list(Pageable p){ var me = CurrentUser.get(); UUID t = TenantContext.require();
    var page = me.role() == Role.FIELD_WORKER ? repo.findByTenantIdAndAuthorId(t, me.id(), p) : repo.findByTenantId(t, p); return page.map(WorkLogService::dto); }
  /** Compteurs par statut (utilisés par l'extension navigateur) ; un collaborateur terrain ne compte que ses propres prestations. */
  @Transactional(readOnly=true) public java.util.Map<String, Long> summary(){ var me = CurrentUser.get(); UUID t = TenantContext.require();
    var rows = me.role() == Role.FIELD_WORKER ? repo.countByStatusForAuthor(t, me.id()) : repo.countByStatus(t);
    var out = new java.util.HashMap<String, Long>(); for (Object[] r : rows) out.put(String.valueOf(r[0]), (Long) r[1]); return out; }
  public WorkLogDto create(WorkLogRequest r){ var me = CurrentUser.get(); projects.load(r.projectId());
    var w = new WorkLog(); w.setProjectId(r.projectId()); w.setAuthorId(me.id()); w.setWorkDate(r.workDate()); w.setServiceType(r.serviceType()); w.setDescription(r.description());
    w.setQuantity(r.quantity()); w.setUnit(r.unit()); w.setUnitPrice(r.unitPrice()); return dto(repo.save(w)); }
  public WorkLogDto update(UUID id, WorkLogRequest r){ var w = load(id);
    // Une prestation validée/facturée n'est JAMAIS modifiée silencieusement.
    if (w.getStatus() != WorkLog.Status.DRAFT && w.getStatus() != WorkLog.Status.REJECTED) throw new Exceptions.BusinessRule("Prestation verrouillée : seule une correction tracée (rejet puis nouvelle soumission) est possible");
    if (!w.getAuthorId().equals(CurrentUser.get().id())) throw new Exceptions.Forbidden("Seul l'auteur peut modifier");
    w.setWorkDate(r.workDate()); w.setServiceType(r.serviceType()); w.setDescription(r.description()); w.setQuantity(r.quantity()); w.setUnit(r.unit()); w.setUnitPrice(r.unitPrice()); return dto(w); }
  public WorkLogDto submit(UUID id){ var w = load(id); if (!w.getAuthorId().equals(CurrentUser.get().id())) throw new Exceptions.Forbidden("Seul l'auteur peut soumettre");
    move(w, WorkLog.Status.SUBMITTED, null); w.setRejectionReason(null); return dto(w); }
  public WorkLogDto approve(UUID id, String comment){ var w = load(id); move(w, WorkLog.Status.APPROVED, comment); return dto(w); }
  public WorkLogDto reject(UUID id, String reason){ if (reason == null || reason.isBlank()) throw new Exceptions.BusinessRule("Le motif du rejet est obligatoire");
    var w = load(id); move(w, WorkLog.Status.REJECTED, reason); w.setRejectionReason(reason); return dto(w); }
  private void move(WorkLog w, WorkLog.Status to, String comment){ var from = w.getStatus(); w.transitionTo(to);
    var a = new WorkApproval(); a.setTenantId(w.getTenantId()); a.setWorkLogId(w.getId()); a.setActorId(CurrentUser.get().id()); a.setFromStatus(from.name()); a.setToStatus(to.name()); a.setComment(comment); approvals.save(a);
    audit.log(w.getTenantId(), CurrentUser.get().id(), "WORKLOG_" + to, "WorkLog", w.getId(), comment); }
  private WorkLog load(UUID id){ return repo.findByIdAndTenantId(id, TenantContext.require()).orElseThrow(() -> new Exceptions.NotFound("Prestation introuvable")); }
  static WorkLogDto dto(WorkLog w){ return new WorkLogDto(w.getId(), w.getProjectId(), w.getAuthorId(), w.getWorkDate(), w.getServiceType(), w.getDescription(), w.getQuantity(), w.getUnit(), w.getUnitPrice(), w.getInvoicedQuantity(), w.getStatus(), w.getRejectionReason()); }
}
