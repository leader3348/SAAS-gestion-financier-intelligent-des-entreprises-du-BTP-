package com.mizanflow.security;
import org.springframework.beans.factory.annotation.Value; import org.springframework.context.annotation.*; import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy; import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain; import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; import org.springframework.web.cors.*; import java.util.List;
@Configuration @EnableMethodSecurity
public class SecurityConfig {
  // Les filtres ne doivent s'exécuter que dans la chaîne Spring Security (pas une 2e fois comme filtres servlet).
  @Bean org.springframework.boot.web.servlet.FilterRegistrationBean<JwtAuthFilter> jwtReg(JwtAuthFilter f){ var r = new org.springframework.boot.web.servlet.FilterRegistrationBean<>(f); r.setEnabled(false); return r; }
  @Bean org.springframework.boot.web.servlet.FilterRegistrationBean<RateLimitFilter> rlReg(RateLimitFilter f){ var r = new org.springframework.boot.web.servlet.FilterRegistrationBean<>(f); r.setEnabled(false); return r; }
  @Bean PasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder(12); }
  @Bean SecurityFilterChain chain(HttpSecurity http, JwtAuthFilter jwt, RateLimitFilter rl) throws Exception {
    http.csrf(c -> c.disable()) // Les routes métier restent protégées par Bearer ; le refresh web utilise un cookie SameSite=Strict.
      .cors(c -> {}).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .headers(h -> h.contentTypeOptions(o -> {}).frameOptions(f -> f.deny()))
      .authorizeHttpRequests(a -> a.requestMatchers("/api/v1/auth/**", "/actuator/health", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
        .requestMatchers("/api/v1/admin/**").hasRole("SUPER_ADMIN").anyRequest().authenticated())
      .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
      .addFilterBefore(rl, UsernamePasswordAuthenticationFilter.class).addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class);
    return http.build(); }
  @Bean CorsConfigurationSource cors(@Value("${mizanflow.cors.allowed-origins}") List<String> origins){
    var c = new CorsConfiguration(); c.setAllowedOriginPatterns(origins); c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE")); c.setAllowedHeaders(List.of("Authorization","Content-Type")); c.setAllowCredentials(true);
    var s = new UrlBasedCorsConfigurationSource(); s.registerCorsConfiguration("/api/**", c); return s; }
}
