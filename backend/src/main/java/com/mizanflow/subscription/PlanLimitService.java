package com.mizanflow.subscription;
import com.mizanflow.common.Exceptions; import com.mizanflow.project.ProjectRepository; import com.mizanflow.user.UserRepository;
import org.springframework.stereotype.Service; import java.time.Instant; import java.util.UUID;
/** Application des quotas CÔTÉ BACKEND (le frontend n'est jamais une garantie). */
@Service
public class PlanLimitService {
  private final SubscriptionRepository subs; private final PlanRepository plans; private final UserRepository users; private final ProjectRepository projects;
  public PlanLimitService(SubscriptionRepository s, PlanRepository p, UserRepository u, ProjectRepository pr){ subs=s; plans=p; users=u; projects=pr; }
  public Plan activePlan(UUID tenant){
    Subscription s = subs.findFirstByTenantIdOrderByCreatedAtDesc(tenant).orElseThrow(() -> new Exceptions.QuotaExceeded("Aucun abonnement"));
    if (!s.isUsable(Instant.now())) throw new Exceptions.QuotaExceeded("Abonnement expiré ou suspendu : renouvelez pour continuer");
    return plans.findById(s.getPlanCode()).orElseThrow(); }
  public void assertCanAddUser(UUID tenant){ Plan p = activePlan(tenant);
    if (users.countActive(tenant) >= p.getMaxUsers()) throw new Exceptions.QuotaExceeded("Limite d'utilisateurs atteinte (" + p.getMaxUsers() + ") pour le plan " + p.getName()); }
  public void assertCanAddProject(UUID tenant){ Plan p = activePlan(tenant);
    if (projects.countActive(tenant) >= p.getMaxActiveProjects()) throw new Exceptions.QuotaExceeded("Limite de projets actifs atteinte (" + p.getMaxActiveProjects() + ") pour le plan " + p.getName()); }
}
