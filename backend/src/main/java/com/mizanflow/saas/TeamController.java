package com.mizanflow.saas;
import com.mizanflow.audit.AuditService; import com.mizanflow.auth.RefreshTokenRepository; import com.mizanflow.common.Exceptions; import com.mizanflow.security.CurrentUser; import com.mizanflow.subscription.PlanLimitService; import com.mizanflow.tenant.TenantContext; import com.mizanflow.user.*;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import org.springframework.http.HttpStatus; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.security.authentication.BadCredentialsException; import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*; import java.security.SecureRandom; import java.util.*;
@RestController @RequestMapping("/api/v1") @Transactional
public class TeamController {
  public record MemberDto(UUID id, String email, String fullName, Role role, boolean active) {}
  public record InviteRequest(@Email @NotBlank String email, @NotBlank @Size(max=120) String fullName, @NotNull Role role) {}
  public record Invited(MemberDto member, String temporaryPassword) {}
  public record PasswordChange(@NotBlank String current, @NotBlank @Size(min=10, max=100) String next) {}
  private static final SecureRandom RNG = new SecureRandom(); private static final String ALPHA = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
  private final UserRepository users; private final PasswordEncoder enc; private final PlanLimitService limits; private final AuditService audit; private final RefreshTokenRepository rts;
  public TeamController(UserRepository u, PasswordEncoder e, PlanLimitService l, AuditService a, RefreshTokenRepository r){ users=u; enc=e; limits=l; audit=a; rts=r; }
  static MemberDto dto(User u){ return new MemberDto(u.getId(), u.getEmail(), u.getFullName(), u.getRole(), u.isActive()); }
  @GetMapping("/team") @PreAuthorize("hasRole('COMPANY_ADMIN')") @Transactional(readOnly=true) List<MemberDto> list(){ return users.findByTenantIdOrderByCreatedAtAsc(TenantContext.require()).stream().map(TeamController::dto).toList(); }
  /** Crée un collaborateur avec un mot de passe temporaire affiché UNE seule fois (l'invitation par email nécessite un SMTP configuré). Quota d'utilisateurs appliqué. */
  @PostMapping("/team") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('COMPANY_ADMIN')") Invited invite(@Valid @RequestBody InviteRequest r){ UUID t = TenantContext.require();
    if (r.role() == Role.SUPER_ADMIN) throw new Exceptions.Forbidden("Rôle interdit"); limits.assertCanAddUser(t);
    if (users.existsByEmailIgnoreCase(r.email())) throw new Exceptions.BusinessRule("Cette adresse email est déjà utilisée");
    var sb = new StringBuilder(); for (int i = 0; i < 14; i++) sb.append(ALPHA.charAt(RNG.nextInt(ALPHA.length())));
    var u = new User(); u.setTenantId(t); u.setEmail(r.email().toLowerCase()); u.setFullName(r.fullName()); u.setRole(r.role()); u.setPasswordHash(enc.encode(sb)); u = users.save(u);
    audit.log(t, CurrentUser.get().id(), "USER_INVITED", "User", u.getId(), r.role().name()); return new Invited(dto(u), sb.toString()); }
  @PostMapping("/team/{id}/deactivate") @PreAuthorize("hasRole('COMPANY_ADMIN')") void deactivate(@PathVariable UUID id){ UUID t = TenantContext.require();
    var u = users.findById(id).filter(x -> t.equals(x.getTenantId())).orElseThrow(() -> new Exceptions.NotFound("Utilisateur introuvable"));
    if (u.getId().equals(CurrentUser.get().id())) throw new Exceptions.BusinessRule("Vous ne pouvez pas désactiver votre propre compte"); u.setActive(false); rts.revokeAllForUser(u.getId()); audit.log(t, CurrentUser.get().id(), "USER_DEACTIVATED", "User", id, null); }
  @PutMapping("/account/password") @ResponseStatus(HttpStatus.NO_CONTENT) void changePassword(@Valid @RequestBody PasswordChange r){ var u = users.findById(CurrentUser.get().id()).orElseThrow();
    if (!enc.matches(r.current(), u.getPasswordHash())) throw new BadCredentialsException("bad"); u.setPasswordHash(enc.encode(r.next())); rts.revokeAllForUser(u.getId()); }
}
