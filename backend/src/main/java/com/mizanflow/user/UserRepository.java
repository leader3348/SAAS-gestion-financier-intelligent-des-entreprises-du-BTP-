package com.mizanflow.user;
import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByEmailIgnoreCase(String email);
  java.util.List<User> findByTenantIdOrderByCreatedAtAsc(UUID tenantId);
  boolean existsByEmailIgnoreCase(String email);
  @Query("select count(u) from User u where u.tenantId=:t and u.active=true") long countActive(@Param("t") UUID t);
}
