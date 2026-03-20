package com.example.daj2ee.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Helper component to create and validate JWT tokens used by the application.
 *
 * - Tokens include subject (username), userId and roles claims.
 * - Uses HMAC-SHA256 signing. The signing key is derived from the configured secret by computing a SHA-256
 *   digest to ensure the key length is appropriate.
 *
 * Configuration properties:
 * - app.jwtSecret (String) - secret used to sign tokens
 * - app.jwtExpirationMs (long) - expiration in milliseconds
 */
@Component
public class JwtTokenProvider {

  private static final Logger logger = LoggerFactory.getLogger(
    JwtTokenProvider.class
  );

  private final Key signingKey;
  private final long jwtExpirationMs;

  public JwtTokenProvider(
    @Value("${app.jwtSecret:changeitsecretkeydontuseinprod}") String jwtSecret,
    @Value("${app.jwtExpirationMs:3600000}") long jwtExpirationMs
  ) {
    this.jwtExpirationMs = jwtExpirationMs;
    this.signingKey = deriveKeyFromSecret(jwtSecret);
  }

  /**
   * Generate a JWT token for the provided user principal.
   *
   * @param principal user principal
   * @return signed JWT token string
   */
  public String generateToken(UserPrincipal principal) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + jwtExpirationMs);

    // Store roles as a single comma-separated claim
    String roles = String.join(
      ",",
      principal.getAuthorities().stream().map(Object::toString).toList()
    );

    return Jwts.builder()
      .setSubject(principal.getUsername())
      .claim("roles", roles)
      .claim("userId", principal.getId())
      .setIssuedAt(now)
      .setExpiration(expiry)
      .signWith(signingKey, SignatureAlgorithm.HS256)
      .compact();
  }

  /**
   * Get username (subject) from the token.
   *
   * @param token JWT token
   * @return username if present
   */
  public String getUsernameFromJWT(String token) {
    Claims claims = parseClaims(token);
    return claims != null ? claims.getSubject() : null;
  }

  /**
   * Extract the userId claim from the token.
   *
   * @param token JWT token
   * @return user id or null if not present / not parseable
   */
  public Long getUserIdFromJWT(String token) {
    Claims claims = parseClaims(token);
    if (claims == null) return null;
    Object idObj = claims.get("userId");
    if (idObj == null) return null;
    if (idObj instanceof Number number) {
      return number.longValue();
    }
    if (idObj instanceof String s) {
      try {
        return Long.parseLong(s);
      } catch (NumberFormatException e) {
        logger.debug("userId claim is not a parsable number: {}", s);
        return null;
      }
    }
    return null;
  }

  /**
   * Extract roles from the token. Roles are returned as a list of strings.
   *
   * @param token JWT token
   * @return list of roles (empty list if none)
   */
  public List<String> getRolesFromJWT(String token) {
    Claims claims = parseClaims(token);
    if (claims == null) return Collections.emptyList();
    Object rolesObj = claims.get("roles");
    if (rolesObj == null) return Collections.emptyList();
    if (rolesObj instanceof String s) {
      if (s.isBlank()) return Collections.emptyList();
      return Arrays.stream(s.split(","))
        .map(String::trim)
        .filter(r -> !r.isEmpty())
        .toList();
    }
    // If stored as a collection for some reason
    if (rolesObj instanceof Collection<?> coll) {
      List<String> out = new ArrayList<>();
      for (Object o : coll) {
        if (o != null) out.add(o.toString());
      }
      return out;
    }
    return Collections.emptyList();
  }

  /**
   * Validate the token's signature and expiry.
   *
   * @param authToken JWT token string
   * @return true if valid
   */
  public boolean validateToken(String authToken) {
    try {
      Jwts.parserBuilder()
        .setSigningKey(signingKey)
        .build()
        .parseClaimsJws(authToken);
      return true;
    } catch (ExpiredJwtException ex) {
      logger.debug("JWT expired: {}", ex.getMessage());
    } catch (UnsupportedJwtException ex) {
      logger.debug("Unsupported JWT: {}", ex.getMessage());
    } catch (MalformedJwtException ex) {
      logger.debug("Invalid JWT: {}", ex.getMessage());
    } catch (SignatureException ex) {
      logger.debug("Invalid JWT signature: {}", ex.getMessage());
    } catch (IllegalArgumentException ex) {
      logger.debug("JWT claims string is empty: {}", ex.getMessage());
    } catch (JwtException ex) {
      logger.debug("JWT processing failed: {}", ex.getMessage());
    }
    return false;
  }

  /**
   * Parse claims from token, returning null when token is invalid or cannot be parsed.
   */
  private Claims parseClaims(String token) {
    try {
      return Jwts.parserBuilder()
        .setSigningKey(signingKey)
        .build()
        .parseClaimsJws(token)
        .getBody();
    } catch (JwtException ex) {
      logger.debug("Failed to parse JWT claims: {}", ex.getMessage());
      return null;
    }
  }

  /**
   * Derive a signing key from the provided secret. To ensure appropriate key length we take a SHA-256 digest
   * of the secret and use it as the HMAC key material.
   */
  private Key deriveKeyFromSecret(String secret) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest(secret.getBytes(StandardCharsets.UTF_8));
      return Keys.hmacShaKeyFor(digest);
    } catch (NoSuchAlgorithmException e) {
      // SHA-256 should always be available; if not, rethrow as unchecked
      throw new IllegalStateException(
        "Unable to initialize JWT signing key",
        e
      );
    } catch (IllegalArgumentException e) {
      // Keys.hmacShaKeyFor will throw IllegalArgumentException for invalid key sizes
      logger.warn(
        "Provided JWT secret resulted in an invalid key size: {}. Reason: {}",
        secret,
        e.getMessage()
      );
      // Best-effort fallback: use the secret bytes directly padded/truncated to 32 bytes
      byte[] src = secret.getBytes(StandardCharsets.UTF_8);
      byte[] padded = new byte[32];
      System.arraycopy(src, 0, padded, 0, Math.min(src.length, padded.length));
      return Keys.hmacShaKeyFor(padded);
    }
  }

  /**
   * Expose expiration for consumers.
   */
  public long getJwtExpirationMs() {
    return jwtExpirationMs;
  }
}
