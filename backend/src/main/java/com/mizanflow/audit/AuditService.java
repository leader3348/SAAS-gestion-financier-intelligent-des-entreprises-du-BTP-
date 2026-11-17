package com.mizanflow.audit;
import org.springframework.stereotype.Service; import java.util.UUID;
@Service
public class AuditService { private final AuditLogRepository repo; public AuditService(AuditLogRepository r){ repo=r; }
  public void log(UUID tenant, UUID actor, String action, String type, UUID entity, String details){
    var a = new AuditLog(); a.setTenantId(tenant); a.setActorId(actor); a.setAction(action); a.setEntityType(type); a.setEntityId(entity); a.setDetails(details); repo.save(a); } }
