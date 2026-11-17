package com.mizanflow.invoice;
import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface PaymentRepository extends JpaRepository<Payment, UUID> { Page<Payment> findByTenantId(UUID t, Pageable p); List<Payment> findByInvoiceIdAndTenantIdOrderByPaidOnAsc(UUID i, UUID t); }
