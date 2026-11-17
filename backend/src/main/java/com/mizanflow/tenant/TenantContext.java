package com.mizanflow.tenant;
import java.util.*;
/** Le tenant provient UNIQUEMENT du JWT signé par le serveur, jamais d'un paramètre client. */
public final class TenantContext {
  private static final ThreadLocal<UUID> CURRENT = new ThreadLocal<>();
  private TenantContext(){}
  public static void set(UUID t){ CURRENT.set(t); }
  public static Optional<UUID> get(){ return Optional.ofNullable(CURRENT.get()); }
  public static UUID require(){ return get().orElseThrow(() -> new IllegalStateException("Aucun tenant dans le contexte")); }
  public static void clear(){ CURRENT.remove(); }
}
