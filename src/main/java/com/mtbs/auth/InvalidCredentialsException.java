package com.mtbs.auth;

/** Thrown on a failed login (unknown email or wrong password). Mapped to HTTP 401. */
public class InvalidCredentialsException extends RuntimeException {

  public InvalidCredentialsException() {
    super("Invalid email or password");
  }
}
