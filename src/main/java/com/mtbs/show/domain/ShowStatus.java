package com.mtbs.show.domain;

/** Lifecycle of a show itself (distinct from {@link ShowSeatStatus}, which is per-seat). */
public enum ShowStatus {
  SCHEDULED,
  CANCELLED
}
