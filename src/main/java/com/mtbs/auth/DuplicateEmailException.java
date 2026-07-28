package com.mtbs.auth;

/** Thrown when registering an email that already exists. Mapped to HTTP 409. */
public class DuplicateEmailException extends RuntimeException {

  public DuplicateEmailException(String email) {
    super("Email already registered: " + email);
  }
}
