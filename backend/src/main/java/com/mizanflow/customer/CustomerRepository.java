package com.mizanflow.customer;
import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
  Optional<Customer> findByIdAndTenantId(UUID id, UUID tenantId);
  @Query("select c from Customer c where c.tenantId=:t and (:q is null or lower(c.name) like lower(concat('%',cast(:q as string),'%')) or lower(coalesce(c.ice,'')) like lower(concat('%',cast(:q as string),'%')) or lower(coalesce(c.city,'')) like lower(concat('%',cast(:q as string),'%')))")
  Page<Customer> search(@Param("t") UUID t, @Param("q") String q, Pageable p); }
