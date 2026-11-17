package com.mizanflow.auth;
import org.springframework.data.jpa.repository.*; import java.util.*;
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
  Optional<RefreshToken> findByTokenHash(String hash);
  @Modifying @Query("update RefreshToken r set r.revoked=true where r.userId=?1") int revokeAllForUser(UUID userId); }
