package com.mizanflow.saas;
import com.mizanflow.user.*; import org.springframework.beans.factory.annotation.Value; import org.springframework.boot.*; import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.stereotype.Component;
/** Crée le SUPER_ADMIN initial UNIQUEMENT si BOOTSTRAP_ADMIN_EMAIL et BOOTSTRAP_ADMIN_PASSWORD (>= 12 car.) sont fournis. Aucun compte par défaut. */
@Component
class BootstrapAdmin implements ApplicationRunner {
  private final UserRepository users; private final PasswordEncoder enc; private final String email, pwd;
  BootstrapAdmin(UserRepository u, PasswordEncoder e, @Value("${BOOTSTRAP_ADMIN_EMAIL:}") String em, @Value("${BOOTSTRAP_ADMIN_PASSWORD:}") String pw){ users=u; enc=e; email=em; pwd=pw; }
  @Override public void run(ApplicationArguments a){
    if (email.isBlank() || pwd.length() < 12 || users.existsByEmailIgnoreCase(email)) return;
    var u = new User(); u.setEmail(email.toLowerCase()); u.setFullName("Administrateur MizanFlow"); u.setPasswordHash(enc.encode(pwd)); u.setRole(Role.SUPER_ADMIN); users.save(u); }
}
