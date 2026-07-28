package com.mtbs.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Issues and validates HS256 JWTs. The subject is the user's email and a {@code role} claim carries
 * the authority. Parsing rejects tampered or expired tokens by throwing, which the authentication
 * filter treats as "unauthenticated".
 */
@Service
public class JwtService {

  private final SecretKey key;
  private final long expirationMs;

  public JwtService(
      @Value("${app.jwt.secret}") String base64Secret,
      @Value("${app.jwt.expiration-ms}") long expirationMs) {
    this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret));
    this.expirationMs = expirationMs;
  }

  public String generateToken(String email, String role) {
    Date now = new Date();
    return Jwts.builder()
        .subject(email)
        .claim("role", role)
        .issuedAt(now)
        .expiration(new Date(now.getTime() + expirationMs))
        .signWith(key)
        .compact();
  }

  /**
   * Parses and validates the token, returning its claims. Throws if the signature is invalid or the
   * token has expired.
   */
  public Claims parse(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
