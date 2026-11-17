package com.mizanflow.worklog;
import jakarta.persistence.LockModeType; import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface WorkLogRepository extends JpaRepository<WorkLog, UUID> {
  Optional<WorkLog> findByIdAndTenantId(UUID id, UUID tenantId);
  Page<WorkLog> findByTenantId(UUID t, Pageable p);
  Page<WorkLog> findByTenantIdAndAuthorId(UUID t, UUID authorId, Pageable p);
  @Query("select w.status, count(w) from WorkLog w where w.tenantId=:t group by w.status") List<Object[]> countByStatus(@Param("t") UUID t);
  @Query("select w.status, count(w) from WorkLog w where w.tenantId=:t and w.authorId=:a group by w.status") List<Object[]> countByStatusForAuthor(@Param("t") UUID t, @Param("a") UUID a);
  @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select w from WorkLog w where w.id in :ids and w.tenantId=:t") List<WorkLog> lockAll(@Param("ids") Collection<UUID> ids, @Param("t") UUID t); }
