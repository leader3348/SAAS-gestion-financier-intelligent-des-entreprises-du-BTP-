package com.mizanflow.auth;
import com.mizanflow.audit.AuditService; import com.mizanflow.common.Exceptions; import com.mizanflow.company.*; import com.mizanflow.security.*;
import com.mizanflow.subscription.*; import com.mizanflow.user.*; import org.springframework.beans.factory.annotation.Value; import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets; import java.security.*; import java.time.*; import java.util.*;
@Service
public class AuthService {
  private final UserRepository users; private final CompanyRepository companies; private final SubscriptionRepository subs; private final RefreshTokenRepository rts;
  private final PasswordEncoder enc; private final JwtService jwt; private final JwtProperties props; private final AuditService audit; private final int trialDays;
  private static final SecureRandom RNG = new SecureRandom();
  public AuthService(UserRepository u, CompanyRepository c, SubscriptionRepository s, RefreshTokenRepository r, PasswordEncoder e, JwtService j, JwtProperties p, AuditService a, @Value("${mizanflow.trial-days}") int td){
    users=u; companies=c; subs=s; rts=r; enc=e; jwt=j; props=p; audit=a; trialDays=td; }

  @Transactional public AuthDtos.TokenResponse register(AuthDtos.RegisterRequest r){
    if (users.existsByEmailIgnoreCase(r.email())) throw new Exceptions.BusinessRule("Cette adresse email est déjà utilisée");
    var c = new Company(); c.setName(r.companyName()); c.setEmail(r.email()); c = companies.save(c);
    var u = new User(); u.setTenantId(c.getId()); u.setEmail(r.email().toLowerCase()); u.setFullName(r.fullName()); u.setPasswordHash(enc.encode(r.password())); u.setRole(Role.COMPANY_ADMIN); u = users.save(u);
    var s = new Subscription(); s.setTenantId(c.getId()); s.setPlanCode("STARTER"); s.setStatus(Subscription.Status.TRIAL); s.setTrialEndsAt(Instant.now().plus(Duration.ofDays(trialDays))); subs.save(s);
    audit.log(c.getId(), u.getId(), "COMPANY_REGISTERED", "Company", c.getId(), null);
    return issue(u); }

  @Transactional public AuthDtos.TokenResponse login(AuthDtos.LoginRequest r){
    var u = users.findByEmailIgnoreCase(r.email()).orElse(null);
    // comparaison BCrypt même si l'utilisateur n'existe pas (limite l'énumération par timing)
    boolean ok = enc.matches(r.password(), u == null ? "$2a$12$invalidinvalidinvalidinvalidinvalidinvalidinvalidinvalidi" : u.getPasswordHash());
    if (u == null || !ok || !u.isActive()) throw new BadCredentialsException("bad");
    if (u.getTenantId() != null && companies.findById(u.getTenantId()).map(x -> x.getStatus() != Company.Status.ACTIVE).orElse(true)) throw new BadCredentialsException("bad");
    audit.log(u.getTenantId(), u.getId(), "LOGIN", "User", u.getId(), null);
    return issue(u); }

  /** Rotation : l'ancien jeton est révoqué. Réutilisation d'un jeton révoqué => toutes les sessions de l'utilisateur sont révoquées (vol présumé). */
  @Transactional(noRollbackFor = BadCredentialsException.class) public AuthDtos.TokenResponse refresh(String raw){
    var t = rts.findByTokenHash(hash(raw)).orElseThrow(() -> new BadCredentialsException("bad"));
    if (t.isRevoked()) { rts.revokeAllForUser(t.getUserId()); throw new BadCredentialsException("reuse"); }
    if (t.getExpiresAt().isBefore(Instant.now())) throw new BadCredentialsException("expired");
    t.setRevoked(true);
    var u = users.findById(t.getUserId()).filter(User::isActive).orElseThrow(() -> new BadCredentialsException("bad"));
    return issue(u); }

  @Transactional public void logout(String raw){ rts.findByTokenHash(hash(raw)).ifPresent(t -> t.setRevoked(true)); }

  private AuthDtos.TokenResponse issue(User u){
    byte[] b = new byte[32]; RNG.nextBytes(b); String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    var t = new RefreshToken(); t.setUserId(u.getId()); t.setTokenHash(hash(raw)); t.setExpiresAt(Instant.now().plus(Duration.ofDays(props.refreshTtlDays()))); rts.save(t);
    return new AuthDtos.TokenResponse(jwt.accessToken(u), raw, jwt.accessTtlSeconds(), u.getRole().name(), u.getFullName()); }
  static String hash(String raw){ try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8))); } catch (NoSuchAlgorithmException e){ throw new IllegalStateException(e);} }
}
