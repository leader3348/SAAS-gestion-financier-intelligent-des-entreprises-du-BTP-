package com.mizanflow.project;
import com.mizanflow.common.Exceptions; import com.mizanflow.customer.CustomerService; import com.mizanflow.subscription.PlanLimitService; import com.mizanflow.tenant.TenantContext;
import jakarta.validation.constraints.*; import org.springframework.data.domain.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal; import java.time.*; import java.util.UUID;
@Service @Transactional
public class ProjectService {
  public record ProjectRequest(@NotBlank @Size(max=200) String name, @NotNull UUID customerId, String description, String siteAddress, UUID managerId,
    LocalDate startDate, LocalDate plannedEndDate, @NotNull @DecimalMin("0") BigDecimal budget, Project.Status status) {}
  public record ProjectDto(UUID id, String reference, String name, UUID customerId, String siteAddress, LocalDate startDate, LocalDate plannedEndDate, BigDecimal budget, Project.Status status) {}
  private static final java.util.Set<Project.Status> ACTIVE = java.util.Set.of(Project.Status.PLANNED, Project.Status.IN_PROGRESS, Project.Status.ON_HOLD);
  private final ProjectRepository repo; private final CustomerService customers; private final PlanLimitService limits;
  public ProjectService(ProjectRepository r, CustomerService c, PlanLimitService l){ repo=r; customers=c; limits=l; }
  @Transactional(readOnly=true) public Page<ProjectDto> list(Pageable p){ return repo.findByTenantId(TenantContext.require(), p).map(ProjectService::dto); }
  @Transactional(readOnly=true) public ProjectDto get(UUID id){ return dto(load(id)); }
  public ProjectDto create(ProjectRequest r){
    UUID t = TenantContext.require(); customers.load(r.customerId()); // le client doit appartenir au même tenant
    var status = r.status()==null ? Project.Status.DRAFT : r.status();
    if (ACTIVE.contains(status)) limits.assertCanAddProject(t); else limits.activePlan(t);
    var p = new Project(); p.setReference("PRJ-" + Year.now().getValue() + "-" + String.format("%04d", repo.countByTenantId(t) + 1)); apply(p, r, status); return dto(repo.save(p)); }
  public ProjectDto update(UUID id, ProjectRequest r){
    var p = load(id); customers.load(r.customerId()); var status = r.status()==null ? p.getStatus() : r.status();
    if (ACTIVE.contains(status) && !ACTIVE.contains(p.getStatus())) limits.assertCanAddProject(p.getTenantId());
    apply(p, r, status); return dto(p); }
  public Project load(UUID id){ return repo.findByIdAndTenantId(id, TenantContext.require()).orElseThrow(() -> new Exceptions.NotFound("Projet introuvable")); }
  private void apply(Project p, ProjectRequest r, Project.Status s){
    if (r.startDate()!=null && r.plannedEndDate()!=null && r.plannedEndDate().isBefore(r.startDate())) throw new Exceptions.BusinessRule("La date de fin prévue précède la date de début");
    p.setName(r.name()); p.setCustomerId(r.customerId()); p.setDescription(r.description()); p.setSiteAddress(r.siteAddress()); p.setManagerId(r.managerId());
    p.setStartDate(r.startDate()); p.setPlannedEndDate(r.plannedEndDate()); p.setBudget(r.budget()); p.setStatus(s); }
  static ProjectDto dto(Project p){ return new ProjectDto(p.getId(), p.getReference(), p.getName(), p.getCustomerId(), p.getSiteAddress(), p.getStartDate(), p.getPlannedEndDate(), p.getBudget(), p.getStatus()); }
}
