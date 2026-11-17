package com.mizanflow.security;
import com.mizanflow.user.Role; import java.util.UUID;
/** Principal authentifié : recharge depuis la base à chaque requête (rôle, statut). */
public record AuthUser(UUID id, UUID tenantId, Role role, String fullName) {}
