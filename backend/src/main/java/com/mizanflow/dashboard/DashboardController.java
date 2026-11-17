package com.mizanflow.dashboard;
import com.mizanflow.invoice.InvoiceRepository; import com.mizanflow.project.ProjectRepository; import com.mizanflow.tenant.TenantContext;
import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*; import java.math.BigDecimal; import java.time.*;
/** Indicateurs calculés uniquement à partir des données réelles du tenant authentifié. */
@RestController @RequestMapping("/api/v1/dashboard") @PreAuthorize("hasAnyRole('COMPANY_ADMIN','PROJECT_MANAGER','ACCOUNTANT')")
public class DashboardController {
  public record Dashboard(long activeProjects, BigDecimal totalInvoiced, BigDecimal totalCollected, BigDecimal totalOutstanding, BigDecimal overdueAmount, long overdueInvoices, String currency) {}
  private final InvoiceRepository inv; private final ProjectRepository prj; public DashboardController(InvoiceRepository i, ProjectRepository p){ inv=i; prj=p; }
  @GetMapping @Transactional(readOnly=true) Dashboard get(){ var t = TenantContext.require(); var today = LocalDate.now(ZoneId.of("Africa/Casablanca"));
    var invoiced = inv.totalInvoiced(t); var collected = inv.totalCollected(t);
    return new Dashboard(prj.countActive(t), invoiced, collected, invoiced.subtract(collected), inv.totalOverdue(t, today), inv.countOverdue(t, today), "MAD"); } }
