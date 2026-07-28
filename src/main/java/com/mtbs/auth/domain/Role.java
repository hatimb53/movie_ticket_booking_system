package com.mtbs.auth.domain;

/**
 * The two roles defined by the requirement. Mapped to Spring Security authorities as
 * {@code ROLE_ADMIN} / {@code ROLE_CUSTOMER}.
 */
public enum Role {
  ADMIN,
  CUSTOMER
}
