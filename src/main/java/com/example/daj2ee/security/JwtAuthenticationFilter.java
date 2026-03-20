package com.example.daj2ee.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that checks for a JWT in the {@code Authorization} header (Bearer token),
 * validates it and, if valid, populates the {@link SecurityContextHolder} with an
 * authenticated {@link UsernamePasswordAuthenticationToken} built directly from
 * JWT claims — no database lookup is performed on every request.
 *
 * This is intentionally lightweight: on token validation failure it simply logs the event
 * and allows the request to proceed so that the security chain can block access where required.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(
    JwtAuthenticationFilter.class
  );

  private final JwtTokenProvider tokenProvider;

  @Autowired
  public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
    this.tokenProvider = tokenProvider;
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    try {
      String jwt = resolveToken(request);
      if (jwt != null && tokenProvider.validateToken(jwt)) {
        String username = tokenProvider.getUsernameFromJWT(jwt);
        Long userId = tokenProvider.getUserIdFromJWT(jwt);
        if (
          username != null &&
          userId != null &&
          SecurityContextHolder.getContext().getAuthentication() == null
        ) {
          List<SimpleGrantedAuthority> authorities = tokenProvider
            .getRolesFromJWT(jwt)
            .stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

          UserPrincipal principal = new UserPrincipal(
            userId,
            username,
            null,
            authorities,
            true
          );

          UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
              principal,
              null,
              authorities
            );
          authentication.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request)
          );
          SecurityContextHolder.getContext().setAuthentication(authentication);
          logger.debug(
            "Set authentication for user '{}' from JWT claims (no DB lookup)",
            username,
            userId
          );
        }
      }
    } catch (Exception ex) {
      // Don't rethrow - we want a failed token validation to result in anonymous request handling.
      logger.debug(
        "Failed to authenticate request using JWT: {}",
        ex.getMessage()
      );
    }

    filterChain.doFilter(request, response);
  }

  /**
   * Extract the JWT token from the {@code Authorization} header. Expects format:
   * Authorization: Bearer &lt;token&gt;
   *
   * @param request current HTTP request
   * @return token string or null if not present
   */
  private String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken)) {
      // Support both standard 'Bearer ' and lowercase 'bearer ' prefixes
      String prefix = "Bearer ";
      if (bearerToken.startsWith(prefix)) {
        return bearerToken.substring(prefix.length());
      }
      String lowerPrefix = "bearer ";
      if (bearerToken.startsWith(lowerPrefix)) {
        return bearerToken.substring(lowerPrefix.length());
      }
    }
    return null;
  }
}
