package com.mizanflow.saas;
import com.mizanflow.audit.AuditService; import com.mizanflow.common.Exceptions; import com.mizanflow.security.CurrentUser; import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*; import java.util.*;
/** Console SaaS : accessible uniquement au rôle SUPER_ADMIN (SecurityConfig : /api/v1/admin/**). */
@RestController @RequestMapping("/api/v1/admin") @Transactional
public class AdminController {
  private final JdbcTemplate jdbc; private final AuditService audit; public AdminController(JdbcTemplate j, AuditService a){ jdbc=j; audit=a; }
  @GetMapping("/stats") @Transactional(readOnly=true) Map<String,Object> stats(){ return jdbc.queryForMap("""
    select (select count(*) from companies) as companies, (select count(*) from companies where status='SUSPENDED') as suspended,
     (select count(*) from subscriptions s where s.status='TRIAL' and s.created_at=(select max(created_at) from subscriptions x where x.tenant_id=s.tenant_id)) as trials,
     (select count(*) from subscription_requests where status='PENDING') as pending_requests,
     (select coalesce(sum(case when s.billing_cycle='YEARLY' then p.price_yearly/12 else p.price_monthly end),0) from subscriptions s join plans p on p.code=s.plan_code
       where s.status='ACTIVE' and s.period_end>now() and s.created_at=(select max(created_at) from subscriptions x where x.tenant_id=s.tenant_id)) as mrr_mad"""); }
  @GetMapping("/companies") @Transactional(readOnly=true) List<Map<String,Object>> companies(){ return jdbc.queryForList("""
    select c.id, c.name, c.status, c.created_at, s.plan_code, s.status as sub_status, s.period_end, s.trial_ends_at, (select count(*) from users u where u.tenant_id=c.id and u.active) as users
    from companies c left join subscriptions s on s.tenant_id=c.id and s.created_at=(select max(created_at) from subscriptions x where x.tenant_id=c.id) order by c.created_at desc limit 500"""); }
  @PostMapping("/companies/{id}/suspend") void suspend(@PathVariable UUID id){ setStatus(id, "SUSPENDED", "COMPANY_SUSPENDED"); }
  @PostMapping("/companies/{id}/reactivate") void reactivate(@PathVariable UUID id){ setStatus(id, "ACTIVE", "COMPANY_REACTIVATED"); }
  private void setStatus(UUID id, String st, String action){ if (jdbc.update("update companies set status=? where id=?", st, id) == 0) throw new Exceptions.NotFound("Entreprise introuvable"); audit.log(id, CurrentUser.get().id(), action, "Company", id, null); }
  @GetMapping("/requests") @Transactional(readOnly=true) List<Map<String,Object>> requests(){ return jdbc.queryForList("select r.id, r.tenant_id, c.name as company, r.plan_code, r.billing_cycle, r.proof_ref, r.created_at from subscription_requests r join companies c on c.id=r.tenant_id where r.status='PENDING' order by r.created_at"); }
  @PostMapping("/requests/{id}/approve") void approve(@PathVariable UUID id){ UUID me = CurrentUser.get().id();
    var r = jdbc.queryForList("select tenant_id, plan_code, billing_cycle, proof_ref from subscription_requests where id=? and status='PENDING' for update", id); if (r.isEmpty()) throw new Exceptions.NotFound("Demande introuvable ou déjà traitée");
    var q = r.get(0); String interval = "YEARLY".equals(q.get("billing_cycle")) ? "1 year" : "1 month";
    jdbc.update("insert into subscriptions (id, tenant_id, plan_code, billing_cycle, status, period_start, period_end, payment_proof_ref, validated_by) values (gen_random_uuid(),?,?,?,'ACTIVE',now(), now() + ?::interval, ?, ?)", q.get("tenant_id"), q.get("plan_code"), q.get("billing_cycle"), interval, q.get("proof_ref"), me);
    jdbc.update("update subscription_requests set status='APPROVED', decided_by=?, decided_at=now() where id=?", me, id); audit.log((UUID) q.get("tenant_id"), me, "SUBSCRIPTION_ACTIVATED", "Plan", null, String.valueOf(q.get("plan_code"))); }
  @PostMapping("/requests/{id}/reject") void reject(@PathVariable UUID id){ if (jdbc.update("update subscription_requests set status='REJECTED', decided_by=?, decided_at=now() where id=? and status='PENDING'", CurrentUser.get().id(), id) == 0) throw new Exceptions.NotFound("Demande introuvable ou déjà traitée"); }
}
