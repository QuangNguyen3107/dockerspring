package com.example.daj2ee.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter that measures and logs the wall-clock time for every HTTP request.
 *
 * Registered as the outermost filter (Order = 1) so it captures the full time
 * including Spring Security, JWT validation, DB access, and serialization.
 *
 * Only active on the "local" profile — excluded from production builds automatically.
 *
 * Example log output:
 *   [TIMING] GET /api/problems -> 200 in 47 ms
 *   [TIMING] GET /api/problems -> 200 in 2543 ms  ← indicates a slow layer
 */
@Component
@Order(1)
@Profile("local")
public class RequestTimingFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(
    RequestTimingFilter.class
  );

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    long startNs = System.nanoTime();
    try {
      filterChain.doFilter(request, response);
    } finally {
      long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
      log.info(
        "[TIMING] {} {} -> {} in {} ms",
        request.getMethod(),
        request.getRequestURI(),
        response.getStatus(),
        elapsedMs
      );
    }
  }
}
