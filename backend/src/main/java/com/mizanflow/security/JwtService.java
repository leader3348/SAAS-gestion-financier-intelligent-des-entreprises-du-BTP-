package com.mizanflow.security;
import com.mizanflow.user.User; import io.jsonwebtoken.*; import io.jsonwebtoken.security.Keys; import org.springframework.stereotype.Service;
import javax.crypto.SecretKey; import java.nio.charset.StandardCharsets; import java.time.*; import java.util.*;
@Service
public class JwtService {
  private final SecretKey key; private final JwtProperties props;
  public JwtService(JwtProperties p){ this.props=p; this.key=Keys.hmacShaKeyFor(p.secret().getBytes(StandardCharsets.UTF_8)); }
  public String accessToken(User u){
    Instant now=Instant.now();
    return Jwts.builder().subject(u.getId().toString()).claim("tid", u.getTenantId()==null?null:u.getTenantId().toString())
      .issuedAt(Date.from(now)).expiration(Date.from(now.plus(Duration.ofMinutes(props.accessTtlMinutes())))).signWith(key).compact(); }
  /** @return l'id utilisateur si le jeton est valide et non expiré. */
  public Optional<UUID> parseUserId(String token){
    try { return Optional.of(UUID.fromString(Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject())); }
    catch (JwtException | IllegalArgumentException e){ return Optional.empty(); } }
  public long accessTtlSeconds(){ return props.accessTtlMinutes()*60; }
}
