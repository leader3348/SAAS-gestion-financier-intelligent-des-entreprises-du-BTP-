package com.mizanflow.security;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimitFilterTest {
  @Test
  void limitsAttemptsPerAddressAndAuthEndpoint() throws ServletException, IOException {
    var filter = new RateLimitFilter(2);
    var chainCalls = new AtomicInteger();

    assertEquals(200, send(filter, "192.0.2.1", "/api/v1/auth/login", chainCalls));
    assertEquals(200, send(filter, "192.0.2.1", "/api/v1/auth/login", chainCalls));
    assertEquals(429, send(filter, "192.0.2.1", "/api/v1/auth/login", chainCalls));
    assertEquals(200, send(filter, "192.0.2.1", "/api/v1/auth/register", chainCalls));
    assertEquals(200, send(filter, "192.0.2.2", "/api/v1/auth/login", chainCalls));
    assertEquals(4, chainCalls.get());
  }

  @Test
  void rejectsNewKeysWhenTheBucketTableIsFull() throws ServletException, IOException {
    var filter = new RateLimitFilter(10);
    var chainCalls = new AtomicInteger();

    for (int i = 0; i < 4_096; i++) {
      assertEquals(200, send(filter, "192.0." + (i / 256) + "." + (i % 256), "/api/v1/auth/login", chainCalls));
    }
    assertEquals(429, send(filter, "198.51.100.1", "/api/v1/auth/login", chainCalls));
    assertEquals(4_096, chainCalls.get());
  }

  private int send(RateLimitFilter filter, String address, String path, AtomicInteger chainCalls)
      throws ServletException, IOException {
    var request = new MockHttpServletRequest("POST", path);
    request.setRemoteAddr(address);
    var response = new MockHttpServletResponse();
    filter.doFilter(request, response, (req, res) -> chainCalls.incrementAndGet());
    return response.getStatus();
  }
}
