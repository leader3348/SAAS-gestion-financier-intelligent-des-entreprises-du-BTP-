package com.mizanflow.security;
import com.mizanflow.company.*; import com.mizanflow.tenant.TenantContext; import com.mizanflow.user.*;
import jakarta.servlet.*; import jakarta.servlet.http.*; import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority; import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component; import org.springframework.web.filter.OncePerRequestFilter; import java.io.IOException; import java.util.List;
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwt; private final UserRepository users; private final CompanyRepository companies;
  public JwtAuthFilter(JwtService j, UserRepository u, CompanyRepository c){ jwt=j; users=u; companies=c; }
  @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
    try {
      String h = req.getHeader("Authorization");
      if (h != null && h.startsWith("Bearer ")) {
        jwt.parseUserId(h.substring(7)).flatMap(users::findById).filter(User::isActive).ifPresent(u -> {
          // Entreprise suspendue => plus aucun accès (sauf SUPER_ADMIN, sans tenant)
          if (u.getTenantId() != null && companies.findById(u.getTenantId()).map(c -> c.getStatus() == Company.Status.ACTIVE).orElse(false) == false) return;
          // Le tenant et le rôle viennent de la BASE, pas du client.
          if (u.getTenantId() != null) TenantContext.set(u.getTenantId());
          var principal = new AuthUser(u.getId(), u.getTenantId(), u.getRole(), u.getFullName());
          SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole()))));
        });
      }
      chain.doFilter(req, res);
    } finally { TenantContext.clear(); SecurityContextHolder.clearContext(); }
  }
}
