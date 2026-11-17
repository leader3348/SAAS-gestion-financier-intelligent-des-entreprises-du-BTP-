package com.mizanflow.saas;
import com.mizanflow.common.Exceptions; import com.mizanflow.project.ProjectRepository; import com.mizanflow.security.CurrentUser; import com.mizanflow.subscription.*; import com.mizanflow.tenant.TenantContext; import com.mizanflow.user.UserRepository;
import com.mizanflow.audit.AuditService; import jakarta.validation.Valid; import jakarta.validation.constraints.*; import org.springframework.dao.DuplicateKeyException; import org.springframework.http.HttpStatus; import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import java.math.BigDecimal; import java.time.Instant; import java.util.*;
@RestController @RequestMapping("/api/v1/billing") @PreAuthorize("hasRole('COMPANY_ADMIN')")
public class BillingController {
  public record PlanDto(String code, String name, BigDecimal priceMonthly, BigDecimal priceYearly, int maxUsers, int maxActiveProjects) {}
  public record SubDto(String planCode, String status, String billingCycle, Instant trialEndsAt, Instant periodEnd, boolean usable, boolean pendingRequest) {}
  public record Usage(long users, long activeProjects) {}
  public record BillingDto(SubDto subscription, List<PlanDto> plans, Usage usage) {}
  public record UpgradeRequest(@NotBlank String planCode, @Pattern(regexp="MONTHLY|YEARLY") String billingCycle, @NotBlank @Size(max=200) String proofRef) {}
  private final SubscriptionRepository subs; private final PlanRepository plans; private final UserRepository users; private final ProjectRepository projects; private final JdbcTemplate jdbc; private final AuditService audit;
  public BillingController(SubscriptionRepository s, PlanRepository p, UserRepository u, ProjectRepository pr, JdbcTemplate j, AuditService a){ subs=s; plans=p; users=u; projects=pr; jdbc=j; audit=a; }
  @GetMapping BillingDto get(){ UUID t = TenantContext.require(); var s = subs.findFirstByTenantIdOrderByCreatedAtDesc(t).orElseThrow(() -> new Exceptions.NotFound("Aucun abonnement"));
    boolean pending = Boolean.TRUE.equals(jdbc.queryForObject("select exists(select 1 from subscription_requests where tenant_id=? and status='PENDING')", Boolean.class, t));
    return new BillingDto(new SubDto(s.getPlanCode(), s.getStatus().name(), s.getBillingCycle(), s.getTrialEndsAt(), s.getPeriodEnd(), s.isUsable(Instant.now()), pending),
      plans.findAll().stream().filter(Plan::isActive).map(p -> new PlanDto(p.getCode(), p.getName(), p.getPriceMonthly(), p.getPriceYearly(), p.getMaxUsers(), p.getMaxActiveProjects())).sorted(Comparator.comparing(PlanDto::priceMonthly)).toList(),
      new Usage(users.countActive(t), projects.countActive(t))); }
  /** Demande d'activation : l'abonnement n'est activé qu'après validation manuelle du justificatif par un SUPER_ADMIN (aucun paiement simulé). */
  @PostMapping("/requests") @ResponseStatus(HttpStatus.CREATED) void request(@Valid @RequestBody UpgradeRequest r){ UUID t = TenantContext.require();
    if (!plans.existsById(r.planCode())) throw new Exceptions.NotFound("Plan inconnu");
    try { jdbc.update("insert into subscription_requests (tenant_id, plan_code, billing_cycle, proof_ref) values (?,?,?,?)", t, r.planCode(), r.billingCycle(), r.proofRef()); }
    catch (DuplicateKeyException e){ throw new Exceptions.BusinessRule("Une demande est déjà en attente de validation"); }
    audit.log(t, CurrentUser.get().id(), "SUBSCRIPTION_REQUESTED", "Plan", null, r.planCode() + "/" + r.billingCycle()); }
}
