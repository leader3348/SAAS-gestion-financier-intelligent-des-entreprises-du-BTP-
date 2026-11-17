package com.mizanflow.invoice;
import jakarta.persistence.LockModeType; import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.math.BigDecimal; import java.time.LocalDate; import java.util.*;
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
  Optional<Invoice> findByIdAndTenantId(UUID id, UUID t);
  Page<Invoice> findByTenantId(UUID t, Pageable p);
  @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select i from Invoice i where i.id=:id and i.tenantId=:t") Optional<Invoice> lockByIdAndTenant(@Param("id") UUID id, @Param("t") UUID t);
  @Query("select count(i) from Invoice i where i.tenantId=:t and i.number like concat(:prefix,'%')") long countNumbered(@Param("t") UUID t, @Param("prefix") String prefix);
  @Query("select coalesce(sum(i.totalTtc),0) from Invoice i where i.tenantId=:t and i.status in ('ISSUED','PARTIALLY_PAID','PAID')") BigDecimal totalInvoiced(@Param("t") UUID t);
  @Query("select coalesce(sum(i.paidAmount),0) from Invoice i where i.tenantId=:t and i.status in ('ISSUED','PARTIALLY_PAID','PAID')") BigDecimal totalCollected(@Param("t") UUID t);
  @Query("select coalesce(sum(i.totalTtc - i.paidAmount),0) from Invoice i where i.tenantId=:t and i.status in ('ISSUED','PARTIALLY_PAID') and i.dueDate < :today") BigDecimal totalOverdue(@Param("t") UUID t, @Param("today") LocalDate today);
  @Query("select count(i) from Invoice i where i.tenantId=:t and i.status in ('ISSUED','PARTIALLY_PAID') and i.dueDate < :today") long countOverdue(@Param("t") UUID t, @Param("today") LocalDate today); }
