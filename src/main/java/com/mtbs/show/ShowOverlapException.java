package com.mtbs.show;

/** Thrown when a show would overlap another show already scheduled on the same screen (including
 * the required buffer). Mapped to HTTP 409. */
public class ShowOverlapException extends RuntimeException {

  public ShowOverlapException(String message) {
    super(message);
  }
}
