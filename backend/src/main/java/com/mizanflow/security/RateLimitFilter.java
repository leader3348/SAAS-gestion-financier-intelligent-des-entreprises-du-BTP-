package com.mizanflow.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** Limiteur simple en mémoire sur les endpoints d'authentification. Pour multi-instances : passer à Redis/Bucket4j. */
@Component
public class RateLimitFilter extends OncePerRequestFilter {
  private static final long WINDOW_MILLIS = 60_000;
  private static final int MAX_BUCKETS = 4_096;

  private final Map<String, ArrayDeque<Long>> hits = new HashMap<>();
  private final int limit;
  private long lastCleanupAt;

  public RateLimitFilter(@Value("${mizanflow.rate-limit.auth-per-minute:10}") int limit) {
    this.limit = limit;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getRequestURI().startsWith("/api/v1/auth/");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String key = request.getRemoteAddr() + request.getRequestURI();
    long now = System.currentTimeMillis();
    boolean blocked;

    synchronized (hits) {
      ArrayDeque<Long> bucket = hits.get(key);
      if (bucket == null && hits.size() >= MAX_BUCKETS) {
        cleanupExpiredBuckets(now);
        if (hits.size() >= MAX_BUCKETS) {
          blocked = true;
        } else {
          bucket = new ArrayDeque<>();
          hits.put(key, bucket);
          blocked = recordAttempt(bucket, now);
        }
      } else {
        if (bucket == null) {
          bucket = new ArrayDeque<>();
          hits.put(key, bucket);
        }
        blocked = recordAttempt(bucket, now);
      }
      if (bucket != null && bucket.isEmpty()) hits.remove(key);
    }

    if (blocked) {
      response.setStatus(429);
      response.setContentType("application/json");
      response.getWriter().write("{\"code\":\"RATE_LIMITED\",\"message\":\"Trop de tentatives\"}");
      return;
    }
    chain.doFilter(request, response);
  }

  private boolean recordAttempt(ArrayDeque<Long> bucket, long now) {
    while (!bucket.isEmpty() && now - bucket.peekFirst() > WINDOW_MILLIS) bucket.pollFirst();
    if (bucket.size() >= limit) return true;
    bucket.addLast(now);
    return false;
  }

  private void cleanupExpiredBuckets(long now) {
    if (now - lastCleanupAt < WINDOW_MILLIS) return;
    lastCleanupAt = now;
    Iterator<Map.Entry<String, ArrayDeque<Long>>> entries = hits.entrySet().iterator();
    while (entries.hasNext()) {
      ArrayDeque<Long> bucket = entries.next().getValue();
      while (!bucket.isEmpty() && now - bucket.peekFirst() > WINDOW_MILLIS) bucket.pollFirst();
      if (bucket.isEmpty()) entries.remove();
    }
  }
}
