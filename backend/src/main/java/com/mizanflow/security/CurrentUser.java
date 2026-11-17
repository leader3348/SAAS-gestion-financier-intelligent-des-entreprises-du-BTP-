package com.mizanflow.security;
import org.springframework.security.core.context.SecurityContextHolder;
public final class CurrentUser { private CurrentUser(){}
  public static AuthUser get(){ return (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal(); } }
