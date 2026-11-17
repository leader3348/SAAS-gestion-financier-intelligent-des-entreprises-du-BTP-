package com.mizanflow.project;
import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface ProjectRepository extends JpaRepository<Project, UUID> {
  Optional<Project> findByIdAndTenantId(UUID id, UUID tenantId);
  Page<Project> findByTenantId(UUID tenantId, Pageable p);
  long countByTenantId(UUID tenantId);
  @Query("select count(p) from Project p where p.tenantId=:t and p.status in ('PLANNED','IN_PROGRESS','ON_HOLD')") long countActive(@Param("t") UUID t); }
