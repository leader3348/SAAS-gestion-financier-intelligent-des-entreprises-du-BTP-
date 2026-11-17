package com.mizanflow.worklog;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface WorkApprovalRepository extends JpaRepository<WorkApproval, UUID> { List<WorkApproval> findByWorkLogIdAndTenantIdOrderByCreatedAtAsc(UUID id, UUID t); }
