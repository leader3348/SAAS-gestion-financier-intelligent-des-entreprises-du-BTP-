package com.mizanflow.common;
public final class Exceptions {
  private Exceptions(){}
  public static class NotFound extends RuntimeException { public NotFound(String m){super(m);} }
  public static class BusinessRule extends RuntimeException { public BusinessRule(String m){super(m);} }
  public static class Forbidden extends RuntimeException { public Forbidden(String m){super(m);} }
  public static class QuotaExceeded extends RuntimeException { public QuotaExceeded(String m){super(m);} }
}
